"use client";

import { ArrowRight, Check, CheckCircle2, HandCoins, Home, X } from "lucide-react";
import { FormEvent, useEffect, useState } from "react";
import BankShell from "../BankShell";
import { InterestSettings, LoanApplication, createLoanApplication, getInterestSettings, getLoanApplications, transferMoney } from "../../lib/api";
import { getSession } from "../../lib/session";

type LoanKind = "personal" | "home";
const products = {
  personal: { name: "Půjčka na cokoliv", rate: 6.9, max: 800000, icon: HandCoins },
  home: { name: "Půjčka na bydlení", rate: 5.4, max: 1500000, icon: Home },
} as const;

const currency = new Intl.NumberFormat("cs-CZ", { style: "currency", currency: "CZK", maximumFractionDigits: 0 });

function monthlyPayment(amount: number, months: number, annualRate: number) {
  const monthlyRate = annualRate / 1200;
  return amount * monthlyRate / (1 - (1 + monthlyRate) ** -months);
}

export default function LoansPage() {
  const [kind, setKind] = useState<LoanKind>("personal");
  const [amount, setAmount] = useState(250000);
  const [months, setMonths] = useState(60);
  const [applicationOpen, setApplicationOpen] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [applications, setApplications] = useState<LoanApplication[]>([]);
  const [loadingApplications, setLoadingApplications] = useState(true);
  const [savingApplication, setSavingApplication] = useState(false);
  const [repaymentLoan, setRepaymentLoan] = useState<LoanApplication | null>(null);
  const [repaymentAmount, setRepaymentAmount] = useState(0);
  const [savingRepayment, setSavingRepayment] = useState(false);
  const [error, setError] = useState("");
  const [rates, setRates] = useState<InterestSettings | null>(null);

  const product = products[kind];
  const productRate = rates ? kind === "personal" ? rates.personalLoanRate : rates.homeLoanRate : product.rate;
  const installment = monthlyPayment(amount, months, productRate);
  const total = installment * months;

  useEffect(() => {
    const session = getSession();
    if (!session) return;
    Promise.all([getLoanApplications(session.id), getInterestSettings()])
      .then(([loadedApplications, loadedRates]) => { setApplications(loadedApplications); setRates(loadedRates); })
      .catch(() => setError("Žádosti se nepodařilo načíst."))
      .finally(() => setLoadingApplications(false));
  }, []);

  function selectProduct(nextKind: LoanKind) {
    setKind(nextKind);
    setAmount((current) => Math.min(current, products[nextKind].max));
  }

  async function submitApplication(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const currentAccountId = getSession()?.id;
    if (!currentAccountId) {
      setError("Přihlášení vypršelo. Přihlaste se znovu.");
      return;
    }
    setSavingApplication(true);
    try {
      const form = new FormData(event.currentTarget);
      const application = await createLoanApplication(currentAccountId, {
        type: kind === "personal" ? "PERSONAL" : "HOME",
        amount,
        repaymentMonths: months,
        purpose: String(form.get("purpose")),
      });
      setApplications((currentApplications) => [application, ...currentApplications]);
      setSubmitted(true);
      setError("");
    } catch (submissionError) {
      setError(submissionError instanceof Error ? submissionError.message : "Žádost se nepodařilo odeslat.");
    } finally {
      setSavingApplication(false);
    }
  }

  function closeApplication() {
    setApplicationOpen(false);
    setSubmitted(false);
  }

  function openRepayment(application: LoanApplication, repayAll: boolean) {
    const remainingPayments = application.remainingInstallments ?? application.repaymentMonths;
    setRepaymentLoan(application);
    setRepaymentAmount(repayAll ? Math.round(application.monthlyPayment * remainingPayments * 100) / 100 : application.monthlyPayment);
    setError("");
  }

  async function submitRepayment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const accountId = getSession()?.id;
    if (!accountId || !repaymentLoan?.repaymentAccountNumber || repaymentAmount <= 0) {
      setError("Splátku se nepodařilo připravit.");
      return;
    }
    setSavingRepayment(true);
    try {
      await transferMoney({
        fromAccountId: accountId,
        toAccountNumber: repaymentLoan.repaymentAccountNumber,
        amount: repaymentAmount,
        description: "Mimořádná splátka půjčky",
        variableSymbol: repaymentLoan.variableSymbol,
        specificSymbol: repaymentLoan.specificSymbol,
      });
      const updatedApplications = await getLoanApplications(accountId);
      setApplications(updatedApplications);
      setRepaymentLoan(null);
      setError("");
    } catch (repaymentError) {
      setError(repaymentError instanceof Error ? repaymentError.message : "Splátku se nepodařilo odeslat.");
    } finally {
      setSavingRepayment(false);
    }
  }

  return <BankShell><div className="bank-content section-page loans-page">
    <div className="page-hero"><div><p className="date-label">Financování podle vašich plánů</p><h1>Půjčky</h1><p className="page-lead">Spočítejte si měsíční splátku a odešlete nezávaznou žádost.</p></div></div>

    <div className="loan-products" role="radiogroup" aria-label="Typ půjčky">
      {(Object.entries(products) as [LoanKind, typeof products[LoanKind]][]).map(([productKind, item]) => { const Icon = item.icon; const rate = rates ? productKind === "personal" ? rates.personalLoanRate : rates.homeLoanRate : item.rate; return <button className={kind === productKind ? "active" : ""} key={productKind} onClick={() => selectProduct(productKind)} role="radio" aria-checked={kind === productKind}><span><Icon size={21} /></span><strong>{item.name}</strong><small>Úrok od {rate.toLocaleString("cs-CZ")} % p. a.</small>{kind === productKind && <Check size={17} />}</button>; })}
    </div>

    <div className="loan-layout">
      <section className="loan-calculator" aria-labelledby="loan-calculator-title">
        <p className="modal-kicker">Kalkulačka</p><h2 id="loan-calculator-title">Nastavte si půjčku</h2>
        <label><span><strong>Kolik si chcete půjčit?</strong><b>{currency.format(amount)}</b></span><input type="range" min="20000" max={product.max} step="10000" value={amount} onChange={(event) => setAmount(Number(event.target.value))} /><small><span>20 000 Kč</span><span>{currency.format(product.max)}</span></small></label>
        <label><span><strong>Jak dlouho chcete splácet?</strong><b>{months} měsíců</b></span><input type="range" min="12" max="120" step="12" value={months} onChange={(event) => setMonths(Number(event.target.value))} /><small><span>1 rok</span><span>10 let</span></small></label>
        <div className="loan-benefits"><span><CheckCircle2 size={17} /> Předčasné splacení zdarma</span><span><CheckCircle2 size={17} /> Peníze po schválení na účet</span></div>
      </section>

      <aside className="loan-summary"><p>Orientační měsíční splátka</p><strong>{currency.format(installment)}</strong><dl><div><dt>Úroková sazba</dt><dd>{productRate.toLocaleString("cs-CZ")} % p. a.</dd></div><div><dt>RPSN</dt><dd>{(productRate + 0.5).toLocaleString("cs-CZ")} %</dd></div><div><dt>Celkem zaplatíte</dt><dd>{currency.format(total)}</dd></div></dl><button className="pay-button" onClick={() => setApplicationOpen(true)}>Požádat o půjčku <ArrowRight size={18} /></button><small>Výpočet je orientační. Finální nabídka závisí na posouzení žádosti.</small></aside>
    </div>

    {error && <p className="api-notice">{error}</p>}
    {loadingApplications && <p className="api-notice">Načítám vaše žádosti...</p>}
    {applications.length > 0 && <section className="loan-applications"><div className="section-heading"><h2>Moje půjčky a žádosti</h2><span>{applications.length}</span></div><div className="loan-table-wrap"><table className="loan-table"><thead><tr><th>Produkt</th><th>Částka a sazba</th><th>Stav splácení</th><th>Termín a symboly</th><th>Stav</th><th>Akce</th></tr></thead><tbody>{applications.map((application) => { const approved = application.status === "APPROVED"; return <tr key={application.id}><td><strong>{products[application.type === "PERSONAL" ? "personal" : "home"].name}</strong><small>Odesláno {new Intl.DateTimeFormat("cs-CZ", { dateStyle: "medium" }).format(new Date(application.createdAt))}</small></td><td><strong>{currency.format(application.amount)}</strong><small>{application.annualRate.toLocaleString("cs-CZ")} % p. a. · {currency.format(application.monthlyPayment)} měsíčně</small></td><td>{approved ? <><strong>Zbývá {currency.format(application.remainingAmount ?? 0)}</strong><small>Uhrazeno {currency.format(application.repaidAmount ?? 0)} · zbývá {application.remainingInstallments ?? application.repaymentMonths} splátek</small></> : <small>Údaje budou dostupné po schválení.</small>}</td><td>{approved ? <><strong>{application.dueDate ? new Intl.DateTimeFormat("cs-CZ").format(new Date(application.dueDate)) : `${application.repaymentDayOfMonth}. den v měsíci`}</strong><small>{application.repaymentAccountNumber}<br />VS {application.variableSymbol} · SS {application.specificSymbol}</small></> : <small>-</small>}</td><td><span className={`loan-status-label ${application.status.toLowerCase()}`}>{application.status === "PENDING" ? "Čeká na posouzení" : application.status === "APPROVED" ? "Schváleno" : "Zamítnuto"}</span></td><td>{approved ? <span className="loan-repayment-actions"><button type="button" onClick={() => openRepayment(application, false)}>Splátka</button><button type="button" onClick={() => openRepayment(application, true)}>Doplatit</button></span> : <small>-</small>}</td></tr>; })}</tbody></table></div></section>}

    {applicationOpen && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && closeApplication()}><section className="payment-modal loan-modal" role="dialog" aria-modal="true" aria-labelledby="loan-application-title"><button className="modal-close" onClick={closeApplication} aria-label="Zavřít"><X size={21} /></button>{submitted ? <div className="payment-success"><span><Check size={30} /></span><h2 id="loan-application-title">Žádost byla odeslána</h2><p>Žádost čeká na schválení v aplikaci Lístek Manager. Peníze budou připsány až po schválení.</p><button className="pay-button" onClick={closeApplication}>Hotovo</button></div> : <><p className="modal-kicker">Nezávazná žádost</p><h2 id="loan-application-title">Zkontrolujte nabídku</h2>{error && <p className="modal-error" role="alert">{error}</p>}<div className="loan-application-summary"><div><span>Produkt</span><strong>{product.name}</strong></div><div><span>Částka</span><strong>{currency.format(amount)}</strong></div><div><span>Splatnost</span><strong>{months} měsíců</strong></div><div><span>Měsíční splátka</span><strong>{currency.format(installment)}</strong></div></div><form onSubmit={submitApplication}><label>Účel půjčky<select name="purpose" required defaultValue=""><option value="" disabled>Vyberte účel</option><option>Vybavení domácnosti</option><option>Auto</option><option>Rekonstrukce</option><option>Jiný účel</option></select></label><label className="loan-consent"><input type="checkbox" required /><span>Souhlasím s posouzením žádosti a ověřením údajů.</span></label><button className="pay-button payment-submit" type="submit" disabled={savingApplication}>{savingApplication ? "Odesílám..." : "Odeslat žádost"}</button></form></>}</section></div>}

    {repaymentLoan && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && setRepaymentLoan(null)}><section className="payment-modal loan-modal" role="dialog" aria-modal="true" aria-labelledby="loan-repayment-title"><button className="modal-close" onClick={() => setRepaymentLoan(null)} aria-label="Zavřít"><X size={21} /></button><p className="modal-kicker">Splácení půjčky</p><h2 id="loan-repayment-title">Odeslat splátku</h2><form onSubmit={submitRepayment}><div className="loan-application-summary"><div><span>Splátkový účet</span><strong>{repaymentLoan.repaymentAccountNumber}</strong></div><div><span>Symboly</span><strong>VS {repaymentLoan.variableSymbol} · SS {repaymentLoan.specificSymbol}</strong></div></div><label>Částka splátky<input type="number" min="1" step="0.01" value={repaymentAmount} onChange={(event) => setRepaymentAmount(Number(event.target.value))} required /></label><button className="pay-button payment-submit" type="submit" disabled={savingRepayment}>{savingRepayment ? "Odesílám..." : `Zaplatit ${currency.format(repaymentAmount)}`}</button></form></section></div>}
  </div></BankShell>;
}