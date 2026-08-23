"use client";

import {
  ArrowDownLeft, ArrowRight, ArrowUpRight, Bell, ChevronDown,
  CreditCard, Eye, EyeOff, FileText, HandCoins, HelpCircle, Home, Landmark,
  LogOut, Menu, Plus, Search, Send, Settings, Smartphone,
  Sparkles, TrendingUp, X,
} from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { getAccounts, getTransactions, Account, BankTransaction, updateAccount } from "../lib/api";
import { clearSession, getSession, setSession } from "../lib/session";
import { FormEvent, useDeferredValue, useEffect, useState } from "react";

type Transaction = {
  id: number;
  title: string;
  detail: string;
  date: string;
  amount: number;
  icon: "card" | "income" | "payment" | "mobile";
  source?: BankTransaction;
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
  { label: "Platby", href: "/payments", icon: Send }, { label: "Karty", href: "/karty", icon: CreditCard },
  { label: "Spoření", href: "/sporeni", icon: TrendingUp }, { label: "Půjčky", href: "/pujcky", icon: HandCoins },
  { label: "Dokumenty", href: "/dokumenty", icon: FileText },
];

const currency = new Intl.NumberFormat("cs-CZ", {
  style: "currency", currency: "CZK", minimumFractionDigits: 2,
});

function TransactionIcon({ type }: { type: Transaction["icon"] }) {
  const Icon = type === "income" ? ArrowDownLeft : type === "mobile" ? Smartphone : type === "payment" ? ArrowUpRight : CreditCard;
  return <Icon size={19} strokeWidth={1.8} />;
}

export default function BankDashboard() {
  const router = useRouter();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [apiTransactions, setApiTransactions] = useState<BankTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [apiError, setApiError] = useState("");
  const [balanceVisible, setBalanceVisible] = useState(true);
  const [search, setSearch] = useState("");
  const [menuOpen, setMenuOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [profileName, setProfileName] = useState("");
  const [profileEmail, setProfileEmail] = useState("");
  const [profileAddress, setProfileAddress] = useState("");
  const [profilePassword, setProfilePassword] = useState("");
  const [profileSaving, setProfileSaving] = useState(false);
  const [profileError, setProfileError] = useState("");
  const [noticeOpen, setNoticeOpen] = useState<"notifications" | "logout" | "spending" | null>(null);
  const [sessionReady, setSessionReady] = useState(false);
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null);
  const deferredSearch = useDeferredValue(search);
  const displayedTransactions: Transaction[] = apiTransactions.length > 0 ? apiTransactions.map((transaction, index) => ({
    id: index,
    title: transaction.description,
    detail: transaction.type === "CREDIT" ? "Příchozí platba" : "Odchozí platba",
    date: new Intl.DateTimeFormat("cs-CZ", { day: "numeric", month: "long" }).format(new Date(transaction.createdAt)),
    amount: transaction.amount,
    icon: transaction.type === "CREDIT" ? "income" : "payment",
    source: transaction,
  })) : transactions;
  const filteredTransactions = displayedTransactions.filter((transaction) =>
    `${transaction.title} ${transaction.detail}`.toLocaleLowerCase("cs").includes(deferredSearch.toLocaleLowerCase("cs")),
  );

  useEffect(() => {
    const session = getSession();
    if (!session) {
      router.replace("/login");
      return;
    }
    getAccounts()
      .then(async (loadedAccounts) => {
        const currentAccounts = loadedAccounts.filter((account) => account.id === session.id);
        const availableAccounts = loadedAccounts.length > 0 ? loadedAccounts : [session];
        setAccounts(availableAccounts);
        const accountTransactions = await Promise.all((currentAccounts.length > 0 ? currentAccounts : [session]).map((account) => getTransactions(account.id)));
        setApiTransactions(accountTransactions.flat().sort((first, second) => new Date(second.createdAt).getTime() - new Date(first.createdAt).getTime()));
      })
      .catch(() => {
        setAccounts([session]);
        setApiError("Backend není dostupný. Zobrazuji poslední známé údaje.");
      })
      .finally(() => { setLoading(false); setSessionReady(true); });
  }, [router]);

  const session = getSession();
  const currentAccount = accounts.find((account) => account.id === session?.id) ?? accounts[0];
  const savingsAccount = accounts.find((account) => account.id !== currentAccount?.id);
  function counterpartyFor(transaction: Transaction) {
    if (!transaction.source) return undefined;
    return apiTransactions.find((candidate) => candidate.id !== transaction.source?.id
      && candidate.accountId !== transaction.source?.accountId
      && candidate.description === transaction.source?.description
      && candidate.amount === -transaction.source?.amount);
  }
  const selectedAccount = selectedTransaction?.source ? accounts.find((account) => account.id === selectedTransaction.source?.accountId) : currentAccount;
  const selectedCounterpartyAccount = selectedTransaction ? accounts.find((account) => account.id === counterpartyFor(selectedTransaction)?.accountId) : undefined;

  function openProfile() {
    setProfileName(currentAccount?.ownerName ?? "");
    setProfileEmail(currentAccount?.email ?? "");
    setProfileAddress(currentAccount?.address ?? "");
    setProfilePassword("");
    setProfileError("");
    setProfileOpen(true);
  }

  async function submitProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!currentAccount || !profileName.trim() || !profileEmail.trim() || !profileAddress.trim()) return;
    setProfileSaving(true);
    try {
      const updatedAccount = await updateAccount(currentAccount.id, { ownerName: profileName.trim(), email: profileEmail.trim(), address: profileAddress.trim(), password: profilePassword || undefined });
      setSession(updatedAccount);
      setAccounts((currentAccounts) => currentAccounts.map((account) => account.id === updatedAccount.id ? updatedAccount : account));
      setProfileEmail(updatedAccount.email);
      setProfileAddress(updatedAccount.address);
      setProfilePassword("");
      setProfileOpen(false);
      setProfileError("");
    } catch (error) {
      setProfileError(error instanceof Error ? error.message : "Profil se nepodařilo uložit.");
    } finally {
      setProfileSaving(false);
    }
  }

  if (!sessionReady) return <div className="session-loading">Načítám bankovnictví...</div>;

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
          <Link href="/help"><HelpCircle size={20} /><span>Pomoc a kontakt</span></Link>
          <Link href="/settings"><Settings size={20} /><span>Nastavení</span></Link>
          <button onClick={() => setNoticeOpen("logout")}><LogOut size={20} /><span>Odhlásit se</span></button>
        </div>
      </aside>
      {menuOpen && <button className="menu-scrim" onClick={() => setMenuOpen(false)} aria-label="Zavřít nabídku" />}

      <main className="bank-main">
        <header className="bank-header">
          <button className="mobile-menu" onClick={() => setMenuOpen(true)} aria-label="Otevřít nabídku"><Menu /></button>
          <div className="mobile-logo">Lístek</div>
          <div className="header-actions">
            <button className="icon-button notification" onClick={() => setNoticeOpen("notifications")} aria-label="Oznámení"><Bell size={20} /><span /></button>
            <button className="profile-button" onClick={openProfile}><span className="avatar">{(currentAccount?.ownerName ?? "Jan Král").split(" ").map((part) => part[0]).slice(0, 2).join("").toUpperCase()}</span><span className="profile-name">{currentAccount?.ownerName ?? "Jan Král"}</span><ChevronDown size={16} /></button>
          </div>
        </header>

        <div className="bank-content">
          {loading && <p className="api-notice">Načítám data z bankovního backendu...</p>}
          {apiError && <p className="api-notice">{apiError}</p>}
          <section className="welcome-row">
            <div><p className="date-label">Čtvrtek, 20. srpna</p><h1>Dobré ráno, Jane.</h1></div>
            <Link className="pay-button" href="/payments"><Plus size={19} /> Nová platba</Link>
          </section>

          <section className="account-section" aria-labelledby="accounts-title">
            <div className="section-heading"><h2 id="accounts-title">Moje účty</h2><Link className="text-button" href="/ucty">Spravovat účty <ArrowRight size={16} /></Link></div>
            <div className="account-grid">
              <article className="account-primary">
                <div className="account-topline"><span className="account-type">Běžný účet</span><button onClick={() => setBalanceVisible((visible) => !visible)} aria-label={balanceVisible ? "Skrýt zůstatek" : "Zobrazit zůstatek"}>{balanceVisible ? <Eye size={20} /> : <EyeOff size={20} />}</button></div>
                <p className="account-number">{currentAccount?.accountNumber ?? "-"}</p><p className="balance-label">Disponibilní zůstatek</p>
                <strong className="main-balance">{balanceVisible ? currentAccount ? currency.format(currentAccount.balance) : currency.format(126840.35) : "••••••••"}</strong>
                <div className="account-footer"><span><i /> Aktivní účet</span><Link href="/ucty">Detail účtu <ArrowRight size={15} /></Link></div>
              </article>
              <article className="savings-account">
                <div className="savings-head"><span><TrendingUp size={19} /> Spořicí účet</span><Link href="/sporeni" aria-label="Detail spořicího účtu"><ArrowRight size={17} /></Link></div>
                <strong>{balanceVisible ? savingsAccount ? currency.format(savingsAccount.balance) : "-" : "••••••••"}</strong><p>Úrok 4,2 % p. a.</p>
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
                  <button className="transaction-row" key={transaction.id} onClick={() => setSelectedTransaction(transaction)}>
                    <span className={`transaction-icon ${transaction.amount > 0 ? "incoming" : ""}`}><TransactionIcon type={transaction.icon} /></span>
                    <span className="transaction-copy"><strong>{transaction.title}</strong><small>{transaction.detail}</small></span>
                    <span className="transaction-date">{transaction.date}</span>
                    <strong className={transaction.amount > 0 ? "amount incoming-amount" : "amount"}>{transaction.amount > 0 ? "+" : ""}{currency.format(transaction.amount)}</strong>
                    <ArrowRight className="row-arrow" size={16} />
                  </button>
                ))}
                {filteredTransactions.length === 0 && <p className="bank-empty">Žádný pohyb neodpovídá hledání.</p>}
              </div>
              <Link className="all-transactions" href="/payments">Všechny pohyby <ArrowRight size={16} /></Link>
            </section>

            <aside className="insights-column">
              <section className="spending-panel">
                <div className="section-heading"><h2>Výdaje v srpnu</h2><button onClick={() => setNoticeOpen("spending")} aria-label="Detail výdajů"><ArrowRight size={17} /></button></div>
                <strong>24 386 Kč</strong><p>O 12 % méně než minulý měsíc</p>
                <div className="spending-bars" aria-label="Výdaje po týdnech">{[42, 68, 50, 82, 58, 72, 44, 63, 37, 54, 31, 47].map((height, index) => <span key={index} style={{ height: `${height}%` }} className={index === 7 ? "current" : ""} />)}</div>
                <div className="bar-labels"><span>1. 8.</span><span>Dnes</span><span>31. 8.</span></div>
              </section>
              <section className="tip-panel"><Sparkles size={21} /><div><strong>Tip pro vaše peníze</strong><p>Na běžném účtu máte víc, než obvykle. Přesuňte část na spoření.</p><Link href="/sporeni">Přesunout peníze <ArrowRight size={15} /></Link></div></section>
            </aside>
          </div>
        </div>
      </main>

      {selectedTransaction && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && setSelectedTransaction(null)}><section className="payment-modal transaction-detail-modal" role="dialog" aria-modal="true" aria-labelledby="dashboard-transaction-detail-title"><button className="modal-close" onClick={() => setSelectedTransaction(null)} aria-label="Zavřít detail"><X size={21} /></button><p className="modal-kicker">Detail pohybu</p><h2 id="dashboard-transaction-detail-title">{selectedTransaction.title}</h2><div className="transaction-detail-amount"><span className={selectedTransaction.amount > 0 ? "incoming-amount" : ""}>{selectedTransaction.amount > 0 ? "+" : "−"}{currency.format(Math.abs(selectedTransaction.amount))}</span><small>{selectedTransaction.detail}</small></div><dl className="transaction-detail-list"><div><dt>Datum</dt><dd>{selectedTransaction.source ? new Intl.DateTimeFormat("cs-CZ", { dateStyle: "long", timeStyle: "short" }).format(new Date(selectedTransaction.source.createdAt)) : selectedTransaction.date}</dd></div><div><dt>Typ pohybu</dt><dd>{selectedTransaction.amount > 0 ? "Příchozí platba" : "Odchozí platba"}</dd></div><div><dt>Odchozí účet</dt><dd>{selectedTransaction.amount < 0 ? selectedAccount?.accountNumber ?? "Neuveden" : selectedCounterpartyAccount?.accountNumber ?? "Neuveden"}</dd></div><div><dt>Cílový účet</dt><dd>{selectedTransaction.amount > 0 ? selectedAccount?.accountNumber ?? "Neuveden" : selectedCounterpartyAccount?.accountNumber ?? "Neuveden"}</dd></div><div><dt>Zpráva</dt><dd>{selectedTransaction.source?.description ?? selectedTransaction.title}</dd></div>{selectedTransaction.source && <div><dt>ID transakce</dt><dd>{selectedTransaction.source.id}</dd></div>}</dl></section></div>}
      {profileOpen && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && setProfileOpen(false)}><section className="payment-modal profile-modal" role="dialog" aria-modal="true" aria-labelledby="dashboard-profile-title"><button className="modal-close" onClick={() => setProfileOpen(false)} aria-label="Zavřít"><X size={21} /></button><p className="modal-kicker">Váš profil</p><h2 id="dashboard-profile-title">Osobní údaje a přihlášení</h2>{profileError && <p className="api-notice">{profileError}</p>}<form onSubmit={submitProfile}><label>Jméno a příjmení<input required minLength={2} maxLength={120} value={profileName} onChange={(event) => setProfileName(event.target.value)} /></label><label>E-mail<input required type="email" value={profileEmail} onChange={(event) => setProfileEmail(event.target.value)} /></label><label>Adresa<input required maxLength={240} value={profileAddress} onChange={(event) => setProfileAddress(event.target.value)} /></label><label>Nové heslo<input type="password" minLength={8} placeholder="Ponechte prázdné, pokud ho neměníte" value={profilePassword} onChange={(event) => setProfilePassword(event.target.value)} /></label><button className="pay-button payment-submit" type="submit" disabled={profileSaving}>{profileSaving ? "Ukládám..." : "Uložit profil"}</button></form></section></div>}
      {noticeOpen && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && setNoticeOpen(null)}><section className="payment-modal compact-modal" role="dialog" aria-modal="true" aria-labelledby="dashboard-notice-title"><button className="modal-close" onClick={() => setNoticeOpen(null)} aria-label="Zavřít"><X size={21} /></button><p className="modal-kicker">{noticeOpen === "spending" ? "Výdaje" : noticeOpen === "notifications" ? "Oznámení" : "Přihlášení"}</p><h2 id="dashboard-notice-title">{noticeOpen === "spending" ? "Odchozí platby" : noticeOpen === "notifications" ? "Vše je v pořádku" : "Odhlásit se?"}</h2>{noticeOpen === "spending" ? <div className="spending-detail-list">{displayedTransactions.filter((transaction) => transaction.amount < 0).slice(0, 5).map((transaction) => <div key={transaction.id}><span>{transaction.title}</span><strong>{currency.format(transaction.amount)}</strong></div>)}</div> : <><p className="modal-copy">{noticeOpen === "notifications" ? "Nemáte žádná nová oznámení." : "Pro další práci s účtem se budete muset znovu přihlásit."}</p><button className="pay-button payment-submit" onClick={() => { if (noticeOpen === "logout") { clearSession(); router.replace("/login"); } else setNoticeOpen(null); }}>{noticeOpen === "logout" ? "Odhlásit se" : "Rozumím"}</button></>}</section></div>}
    </div>
  );
}