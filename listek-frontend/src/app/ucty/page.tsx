"use client";

import { ArrowRight, Eye, EyeOff, Landmark, Plus, TrendingUp } from "lucide-react";
import { useEffect, useState } from "react";
import BankShell from "../BankShell";
import { Account, getAccounts } from "../../lib/api";

export default function AccountsPage() {
  const [visible, setVisible] = useState(true);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    getAccounts().then(setAccounts).catch(() => setError("Backend není dostupný, zobrazuji ukázkové hodnoty."));
  }, []);

  const primaryAccount = accounts[0];
  const savingsAccount = accounts[1];
  return <BankShell><div className="bank-content section-page">
    {error && <p className="api-notice">{error}</p>}
    <div className="page-hero"><div><p className="date-label">Vaše peníze na jednom místě</p><h1>Moje účty</h1><p className="page-lead">Přehled účtů, zůstatků a nastavení vašich financí.</p></div><button className="pay-button"><Plus size={19} /> Přidat účet</button></div>
    <div className="section-heading"><h2>Účty a zůstatky</h2><button className="text-button" onClick={() => setVisible((state) => !state)}>{visible ? <EyeOff size={16} /> : <Eye size={16} />} {visible ? "Skrýt zůstatky" : "Zobrazit zůstatky"}</button></div>
    <div className="account-page-grid">
      <article className="account-primary account-detail-card"><div className="account-topline"><span className="account-type">Běžný účet</span><Landmark size={20} /></div><p className="account-number">{primaryAccount?.accountNumber ?? "123456789 / 3030"}</p><p className="balance-label">Disponibilní zůstatek</p><strong className="main-balance">{visible ? `${(primaryAccount?.balance ?? 126840.35).toLocaleString("cs-CZ", { minimumFractionDigits: 2 })} ${primaryAccount?.currency ?? "Kč"}` : "••••••••"}</strong><div className="account-footer"><span><i /> Aktivní účet</span><button>Detail účtu <ArrowRight size={15} /></button></div></article>
      <article className="savings-account account-detail-card"><div className="savings-head"><span><TrendingUp size={19} /> Spořicí účet</span><button><ArrowRight size={17} /></button></div><strong>{visible ? `${(savingsAccount?.balance ?? 84200).toLocaleString("cs-CZ", { minimumFractionDigits: 2 })} ${savingsAccount?.currency ?? "Kč"}` : "••••••••"}</strong><p>Úrok 4,2 % p. a.</p><div className="saving-progress"><span /></div><small>Cíl: Finanční rezerva <b>84 %</b></small></article>
    </div>
    <section className="settings-panel"><div><h2>Výpisy a nastavení</h2><p>Spravujte limity, notifikace a detaily svých účtů.</p></div><button className="text-button">Otevřít nastavení <ArrowRight size={16} /></button></section>
  </div></BankShell>;
}
