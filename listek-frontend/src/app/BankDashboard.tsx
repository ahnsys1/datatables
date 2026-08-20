"use client";

import {
  ArrowDownLeft, ArrowRight, ArrowUpRight, Bell, Check, ChevronDown,
  CreditCard, Eye, EyeOff, FileText, HelpCircle, Home, Landmark,
  LogOut, Menu, Plus, Search, Send, Settings, ShieldCheck, Smartphone,
  Sparkles, TrendingUp, X,
} from "lucide-react";
import Link from "next/link";
import { getAccounts, getTransactions, Account, BankTransaction } from "../lib/api";
import { FormEvent, useDeferredValue, useEffect, useState } from "react";

type Transaction = {
  id: number;
  title: string;
  detail: string;
  date: string;
  amount: number;
  icon: "card" | "income" | "payment" | "mobile";
};

const transactions: Transaction[] = [
  { id: 1, title: "Výplata", detail: "Bright Studio s.r.o.", date: "Dnes", amount: 48500, icon: "income" },
  { id: 2, title: "Albert", detail: "Platba kartou •• 2841", date: "Dnes", amount: -1248.9, icon: "card" },
  { id: 3, title: "Spotify", detail: "Předplatné •• 2841", date: "Včera", amount: -169, icon: "mobile" },
  { id: 4, title: "Nájem", detail: "Jan Novotný", date: "18. srpna", amount: -18500, icon: "payment" },
  { id: 5, title: "Kavárna Místo", detail: "Platba kartou •• 2841", date: "17. srpna", amount: -178, icon: "card" },
  { id: 6, title: "Vrácení platby", detail: "Alza.cz", date: "16. srpna", amount: 2390, icon: "income" },
];

const navigation = [
  { label: "Přehled", href: "/", icon: Home }, { label: "Účty", href: "/ucty", icon: Landmark },
  { label: "Platby", href: "/platby", icon: Send }, { label: "Karty", href: "/karty", icon: CreditCard },
  { label: "Spoření", href: "/sporeni", icon: TrendingUp }, { label: "Dokumenty", href: "/dokumenty", icon: FileText },
];

const currency = new Intl.NumberFormat("cs-CZ", {
  style: "currency", currency: "CZK", minimumFractionDigits: 2,
});

function TransactionIcon({ type }: { type: Transaction["icon"] }) {
  const Icon = type === "income" ? ArrowDownLeft : type === "mobile" ? Smartphone : type === "payment" ? ArrowUpRight : CreditCard;
  return <Icon size={19} strokeWidth={1.8} />;
}

export default function BankDashboard() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [apiTransactions, setApiTransactions] = useState<BankTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [apiError, setApiError] = useState("");
  const [balanceVisible, setBalanceVisible] = useState(true);
  const [search, setSearch] = useState("");
  const [paymentOpen, setPaymentOpen] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [paymentSent, setPaymentSent] = useState(false);
  const deferredSearch = useDeferredValue(search);
  const displayedTransactions: Transaction[] = apiTransactions.length > 0 ? apiTransactions.map((transaction, index) => ({
    id: index,
    title: transaction.description,
    detail: transaction.type === "CREDIT" ? "Příchozí platba" : "Odchozí platba",
    date: new Intl.DateTimeFormat("cs-CZ", { day: "numeric", month: "long" }).format(new Date(transaction.createdAt)),
    amount: transaction.amount,
    icon: transaction.type === "CREDIT" ? "income" : "payment",
  })) : transactions;
  const filteredTransactions = displayedTransactions.filter((transaction) =>
    `${transaction.title} ${transaction.detail}`.toLocaleLowerCase("cs").includes(deferredSearch.toLocaleLowerCase("cs")),
  );

  useEffect(() => {
    getAccounts()
      .then(async (loadedAccounts) => {
        setAccounts(loadedAccounts);
        if (loadedAccounts[0]) setApiTransactions(await getTransactions(loadedAccounts[0].id));
      })
      .catch(() => setApiError("Backend není dostupný. Zobrazuji ukázková data."))
      .finally(() => setLoading(false));
  }, []);

  const currentAccount = accounts[0];
  const savingsAccount = accounts[1];

  function closePayment() {
    setPaymentOpen(false);
    setPaymentSent(false);
  }

  function submitPayment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPaymentSent(true);
  }

  return (
    <div className="bank-app">
      <aside className={`bank-sidebar ${menuOpen ? "is-open" : ""}`}>
        <div className="bank-logo" aria-label="Lístek banka"><span className="logo-mark"><span /></span><span>Lístek</span></div>
        <button className="sidebar-close" onClick={() => setMenuOpen(false)} aria-label="Zavřít nabídku"><X size={22} /></button>
        <nav className="bank-nav" aria-label="Hlavní navigace">
          {navigation.map(({ label, href, icon: Icon }, index) => (
            <Link className={index === 0 ? "active" : ""} href={href} key={label} onClick={() => setMenuOpen(false)}><Icon size={20} strokeWidth={1.8} /><span>{label}</span></Link>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <button><HelpCircle size={20} /><span>Pomoc a kontakt</span></button>
          <button><Settings size={20} /><span>Nastavení</span></button>
          <button><LogOut size={20} /><span>Odhlásit se</span></button>
        </div>
      </aside>
      {menuOpen && <button className="menu-scrim" onClick={() => setMenuOpen(false)} aria-label="Zavřít nabídku" />}

      <main className="bank-main">
        <header className="bank-header">
          <button className="mobile-menu" onClick={() => setMenuOpen(true)} aria-label="Otevřít nabídku"><Menu /></button>
          <div className="mobile-logo">Lístek</div>
          <div className="header-actions">
            <button className="icon-button notification" aria-label="Oznámení"><Bell size={20} /><span /></button>
            <button className="profile-button"><span className="avatar">JK</span><span className="profile-name">Jan Král</span><ChevronDown size={16} /></button>
          </div>
        </header>

        <div className="bank-content">
          {loading && <p className="api-notice">Načítám data z bankovního backendu...</p>}
          {apiError && <p className="api-notice">{apiError}</p>}
          <section className="welcome-row">
            <div><p className="date-label">Čtvrtek, 20. srpna</p><h1>Dobré ráno, Jane.</h1></div>
            <button className="pay-button" onClick={() => setPaymentOpen(true)}><Plus size={19} /> Nová platba</button>
          </section>

          <section className="account-section" aria-labelledby="accounts-title">
            <div className="section-heading"><h2 id="accounts-title">Moje účty</h2><button className="text-button">Spravovat účty <ArrowRight size={16} /></button></div>
            <div className="account-grid">
              <article className="account-primary">
                <div className="account-topline"><span className="account-type">Běžný účet</span><button onClick={() => setBalanceVisible((visible) => !visible)} aria-label={balanceVisible ? "Skrýt zůstatek" : "Zobrazit zůstatek"}>{balanceVisible ? <Eye size={20} /> : <EyeOff size={20} />}</button></div>
                <p className="account-number">123456789 / 3030</p><p className="balance-label">Disponibilní zůstatek</p>
                <strong className="main-balance">{balanceVisible ? currentAccount ? currency.format(currentAccount.balance) : currency.format(126840.35) : "••••••••"}</strong>
                <div className="account-footer"><span><i /> Aktivní účet</span><button>Detail účtu <ArrowRight size={15} /></button></div>
              </article>
              <article className="savings-account">
                <div className="savings-head"><span><TrendingUp size={19} /> Spořicí účet</span><button aria-label="Detail spořicího účtu"><ArrowRight size={17} /></button></div>
                <strong>{balanceVisible ? savingsAccount ? currency.format(savingsAccount.balance) : currency.format(84200) : "••••••••"}</strong><p>Úrok 4,2 % p. a.</p>
                <div className="saving-progress"><span /></div><small>Cíl: Finanční rezerva <b>84 %</b></small>
              </article>
              <article className="card-preview">
                <div className="card-chip" /><span className="card-brand">Lístek</span><p>••••&nbsp; ••••&nbsp; ••••&nbsp; 2841</p><div><span>JAN KRÁL</span><b>VISA</b></div>
              </article>
            </div>
          </section>

          <div className="dashboard-grid">
            <section className="transactions-section" aria-labelledby="transactions-title">
              <div className="section-heading transaction-heading">
                <div><h2 id="transactions-title">Poslední pohyby</h2><p>Srpen 2026</p></div>
                <label className="transaction-search"><Search size={18} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Hledat" aria-label="Hledat v transakcích" /></label>
              </div>
              <div className="transaction-list">
                {filteredTransactions.map((transaction) => (
                  <button className="transaction-row" key={transaction.id}>
                    <span className={`transaction-icon ${transaction.amount > 0 ? "incoming" : ""}`}><TransactionIcon type={transaction.icon} /></span>
                    <span className="transaction-copy"><strong>{transaction.title}</strong><small>{transaction.detail}</small></span>
                    <span className="transaction-date">{transaction.date}</span>
                    <strong className={transaction.amount > 0 ? "amount incoming-amount" : "amount"}>{transaction.amount > 0 ? "+" : ""}{currency.format(transaction.amount)}</strong>
                    <ArrowRight className="row-arrow" size={16} />
                  </button>
                ))}
                {filteredTransactions.length === 0 && <p className="bank-empty">Žádný pohyb neodpovídá hledání.</p>}
              </div>
              <button className="all-transactions">Všechny pohyby <ArrowRight size={16} /></button>
            </section>

            <aside className="insights-column">
              <section className="spending-panel">
                <div className="section-heading"><h2>Výdaje v srpnu</h2><button aria-label="Detail výdajů"><ArrowRight size={17} /></button></div>
                <strong>24 386 Kč</strong><p>O 12 % méně než minulý měsíc</p>
                <div className="spending-bars" aria-label="Výdaje po týdnech">{[42, 68, 50, 82, 58, 72, 44, 63, 37, 54, 31, 47].map((height, index) => <span key={index} style={{ height: `${height}%` }} className={index === 7 ? "current" : ""} />)}</div>
                <div className="bar-labels"><span>1. 8.</span><span>Dnes</span><span>31. 8.</span></div>
              </section>
              <section className="tip-panel"><Sparkles size={21} /><div><strong>Tip pro vaše peníze</strong><p>Na běžném účtu máte víc, než obvykle. Přesuňte část na spoření.</p><button>Přesunout peníze <ArrowRight size={15} /></button></div></section>
            </aside>
          </div>
        </div>
      </main>

      {paymentOpen && (
        <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && closePayment()}>
          <section className="payment-modal" role="dialog" aria-modal="true" aria-labelledby="payment-title">
            <button className="modal-close" onClick={closePayment} aria-label="Zavřít"><X size={21} /></button>
            {paymentSent ? (
              <div className="payment-success"><span><Check size={30} /></span><h2>Platba je připravená</h2><p>Po potvrzení v mobilní aplikaci ji odešleme.</p><button className="pay-button" onClick={closePayment}>Hotovo</button></div>
            ) : (
              <><p className="modal-kicker">Nová platba</p><h2 id="payment-title">Komu posíláte?</h2>
                <form onSubmit={submitPayment}>
                  <label>Číslo účtu<input required placeholder="123456789 / 0100" /></label>
                  <div className="payment-fields"><label>Částka<input required type="number" min="1" placeholder="0,00" /></label><label>Měna<select defaultValue="CZK"><option>CZK</option><option>EUR</option></select></label></div>
                  <label>Zpráva pro příjemce<input placeholder="Například oběd" /></label>
                  <button className="pay-button payment-submit" type="submit">Pokračovat <ArrowRight size={18} /></button>
                </form>
                <p className="secure-note"><ShieldCheck size={16} /> Platbu před odesláním bezpečně potvrdíte.</p></>
            )}
          </section>
        </div>
      )}
    </div>
  );
}