"use client";

import { ArrowRight, Eye, EyeOff, Landmark, Plus, RefreshCw, TrendingUp, X } from "lucide-react";
import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import BankShell from "../BankShell";
import { Account, createAccount, CurrencyCode, getAccounts } from "../../lib/api";
import { getSession } from "../../lib/session";

export default function AccountsPage() {
  const [visible, setVisible] = useState(true);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [error, setError] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [detailAccount, setDetailAccount] = useState<Account | null>(null);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState("");

  function loadAccounts() {
    setError("");
    return getAccounts().then(setAccounts).catch(() => setError("Účty se nepodařilo načíst."));
  }

  useEffect(() => {
    const session = getSession();
    getAccounts().then((loadedAccounts) => {
      setAccounts(loadedAccounts);
    }).catch(() => setError("Účty se nepodařilo načíst."));
  }, []);

  async function submitAccount(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setFormError("");
    const formData = new FormData(event.currentTarget);
    try {
      await createAccount({
        ownerName: String(formData.get("ownerName")),
        email: String(formData.get("email")),
        address: String(formData.get("address")),
        password: String(formData.get("password")),
        accountNumber: String(formData.get("accountNumber")).replace(/\s/g, ""),
        initialBalance: Number(formData.get("initialBalance") || 0),
        currency: String(formData.get("currency")) as CurrencyCode,
      });
      await loadAccounts();
      setCreateOpen(false);
      event.currentTarget.reset();
    } catch (submissionError) {
      setFormError(submissionError instanceof Error ? submissionError.message : "Účet se nepodařilo vytvořit.");
    } finally {
      setSaving(false);
    }
  }

  const session = getSession();
  const primaryAccount = accounts.find((account) => account.id === session?.id) ?? accounts[0];
  const savingsAccount = accounts.find((account) => account.type === "SAVINGS");
  return <BankShell><div className="bank-content section-page">
    {error && <p className="api-notice">{error}</p>}
    <div className="page-hero"><div><p className="date-label">Vaše peníze na jednom místě</p><h1>Moje účty</h1><p className="page-lead">Přehled účtů, zůstatků a nastavení vašich financí.</p></div><button className="pay-button" onClick={() => { setFormError(""); setCreateOpen(true); }}><Plus size={19} /> Přidat účet</button></div>
    {accounts.length > 0 && <div className="account-management-bar"><span><strong>{accounts.length}</strong> {accounts.length === 1 ? "účet" : "účty"} v přehledu</span><button className="text-button" onClick={() => void loadAccounts()}><RefreshCw size={15} /> Obnovit</button></div>}
    <div className="section-heading"><h2>Účty a zůstatky</h2><button className="text-button" onClick={() => setVisible((state) => !state)}>{visible ? <EyeOff size={16} /> : <Eye size={16} />} {visible ? "Skrýt zůstatky" : "Zobrazit zůstatky"}</button></div>
    <div className="account-page-grid">
      <article className="account-primary account-detail-card"><div className="account-topline"><span className="account-type"><Landmark size={20} /> Běžný účet</span>{primaryAccount && <Link className="row-arrow" href={`/payments?accountId=${primaryAccount.id}`} aria-label="Zobrazit pohyby běžného účtu" title="Zobrazit pohyby běžného účtu"><ArrowRight size={17} /></Link>}</div><p className="account-number">{primaryAccount?.accountNumber ?? "-"}</p><p className="balance-label">Disponibilní zůstatek</p><strong className="main-balance">{visible ? primaryAccount ? `${primaryAccount.balance.toLocaleString("cs-CZ", { minimumFractionDigits: 2 })} ${primaryAccount.currency}` : "-" : "••••••••"}</strong><div className="account-footer"><span><i /> Aktivní účet</span><button onClick={() => primaryAccount && setDetailAccount(primaryAccount)}>Detail účtu <ArrowRight size={15} /></button></div></article>
      {savingsAccount && <article className="savings-account account-detail-card"><div className="savings-head"><span><TrendingUp size={19} /> Spořicí účet</span><Link className="row-arrow" href={`/payments?accountId=${savingsAccount.id}`} aria-label="Zobrazit pohyby spořicího účtu" title="Zobrazit pohyby spořicího účtu"><ArrowRight size={17} /></Link></div><p className="account-number">{savingsAccount.accountNumber}</p><strong>{visible ? `${savingsAccount.balance.toLocaleString("cs-CZ", { minimumFractionDigits: 2 })} ${savingsAccount.currency}` : "••••••••"}</strong><p>Účet je veden v měně {savingsAccount.currency}.</p><div className="saving-progress"><span style={{ width: "0%" }} /></div><small>Spořicí cíl zatím není nastaven.</small><div className="account-footer"><span><i /> Aktivní účet</span><button onClick={() => setDetailAccount(savingsAccount)}>Detail účtu <ArrowRight size={15} /></button></div></article>}
    </div>
    <section className="settings-panel"><div><h2>Výpisy a nastavení</h2><p>Spravujte limity, notifikace a detaily svých účtů.</p></div><Link className="text-button" href="/settings">Otevřít nastavení <ArrowRight size={16} /></Link></section>
    {createOpen && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && setCreateOpen(false)}><section className="payment-modal account-modal" role="dialog" aria-modal="true" aria-labelledby="create-account-title"><button className="modal-close" onClick={() => setCreateOpen(false)} aria-label="Zavřít"><X size={21} /></button><p className="modal-kicker">Nový účet</p><h2 id="create-account-title">Přidat účet</h2>{formError && <p className="api-notice">{formError}</p>}<form onSubmit={submitAccount}><label>Jméno a příjmení<input name="ownerName" required minLength={2} maxLength={120} defaultValue={primaryAccount?.ownerName ?? ""} /></label><label>E-mail<input name="email" required type="email" defaultValue={primaryAccount?.email ?? ""} /></label><label>Adresa<input name="address" required maxLength={240} defaultValue={primaryAccount?.address ?? ""} /></label><label>Číslo účtu<input name="accountNumber" required pattern="[0-9]{9,34}" placeholder="123456789" /></label><div className="payment-fields"><label>Počáteční zůstatek<input name="initialBalance" type="number" min="0" step="0.01" defaultValue="0" /></label><label>Měna<select name="currency" defaultValue="CZK"><option value="CZK">CZK</option><option value="EUR">EUR</option></select></label></div><label>Heslo<input name="password" required type="password" minLength={8} /></label><button className="pay-button payment-submit" type="submit" disabled={saving}>{saving ? "Vytvářím..." : "Vytvořit účet"}</button></form></section></div>}
    {detailAccount && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && setDetailAccount(null)}><section className="payment-modal account-modal" role="dialog" aria-modal="true" aria-labelledby="account-detail-title"><button className="modal-close" onClick={() => setDetailAccount(null)} aria-label="Zavřít"><X size={21} /></button><p className="modal-kicker">Detail účtu</p><h2 id="account-detail-title">{detailAccount.ownerName}</h2><dl className="account-detail-list"><div><dt>Číslo účtu</dt><dd>{detailAccount.accountNumber}</dd></div><div><dt>E-mail</dt><dd>{detailAccount.email}</dd></div><div><dt>Adresa</dt><dd>{detailAccount.address}</dd></div><div><dt>Zůstatek</dt><dd>{detailAccount.balance.toLocaleString("cs-CZ", { style: "currency", currency: detailAccount.currency })}</dd></div></dl></section></div>}
  </div></BankShell>;
}
