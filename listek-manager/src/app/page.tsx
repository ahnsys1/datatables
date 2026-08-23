"use client";

import {
  Bell, Check, ChevronRight, CircleDollarSign, ClipboardCheck, FileClock,
  Landmark, LayoutDashboard, Menu, Search, ShieldCheck, Users, WalletCards, X,
} from "lucide-react";
import { FormEvent, useEffect, useState } from "react";
import {
  Account, BankApplication, Dashboard, InterestSettings, decideApplication, getAccounts,
  getDashboard, getInterestSettings, getLoans, getOverdrafts, updateInterestSettings,
} from "@/lib/api";

type View = "overview" | "loans" | "overdrafts" | "clients" | "settings";

const money = new Intl.NumberFormat("cs-CZ", { style: "currency", currency: "CZK", maximumFractionDigits: 0 });
const date = new Intl.DateTimeFormat("cs-CZ", { dateStyle: "medium", timeStyle: "short" });
const navigation = [
  { id: "overview" as const, label: "Přehled", icon: LayoutDashboard },
  { id: "loans" as const, label: "Půjčky", icon: CircleDollarSign },
  { id: "overdrafts" as const, label: "Kontokorenty", icon: WalletCards },
  { id: "clients" as const, label: "Klienti", icon: Users },
  { id: "settings" as const, label: "Nastavení sazeb", icon: ClipboardCheck },
];

export default function Home() {
  const [view, setView] = useState<View>("overview");
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [loans, setLoans] = useState<BankApplication[]>([]);
  const [overdrafts, setOverdrafts] = useState<BankApplication[]>([]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [selected, setSelected] = useState<BankApplication | null>(null);
  const [search, setSearch] = useState("");
  const [pendingOnly, setPendingOnly] = useState(true);
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [menuOpen, setMenuOpen] = useState(false);
  const [interestSettings, setInterestSettings] = useState<InterestSettings | null>(null);

  useEffect(() => {
    Promise.all([getDashboard(), getLoans(), getOverdrafts(), getAccounts(), getInterestSettings()])
      .then(([dashboardData, loanData, overdraftData, accountData, settingsData]) => {
        setDashboard(dashboardData);
        setLoans(loanData);
        setOverdrafts(overdraftData);
        setAccounts(accountData);
        setInterestSettings(settingsData);
      })
      .catch(() => setError("Administraci se nepodařilo spojit s backendem."))
      .finally(() => setLoading(false));
  }, []);

  const allApplications = [...loans, ...overdrafts].sort((left, right) => right.createdAt.localeCompare(left.createdAt));
  const sourceApplications = view === "loans" ? loans : view === "overdrafts" ? overdrafts : allApplications;
  const visibleApplications = sourceApplications.filter((application) =>
    (!pendingOnly || application.status === "PENDING")
      && (application.clientName.toLocaleLowerCase("cs").includes(search.toLocaleLowerCase("cs"))
        || application.accountNumber.includes(search)));

  async function decide(status: "APPROVED" | "REJECTED") {
    if (!selected) return;
    setSaving(true);
    try {
      const updated = await decideApplication(selected, status, note.trim());
      const updateList = (items: BankApplication[]) => items.map((item) => item.id === updated.id ? updated : item);
      if (updated.category === "LOAN") setLoans(updateList);
      else setOverdrafts(updateList);
      setSelected(updated);
      setNote("");
      setDashboard(await getDashboard());
      setError("");
    } catch (decisionError) {
      setError(decisionError instanceof Error ? decisionError.message : "Rozhodnutí se nepodařilo uložit.");
    } finally {
      setSaving(false);
    }
  }

  const titles: Record<View, [string, string]> = {
    overview: ["Operační přehled", "Dnes máte pod kontrolou vše důležité."],
    loans: ["Žádosti o půjčku", "Posuďte žádosti, riziko a schopnost klienta splácet."],
    overdrafts: ["Žádosti o kontokorent", "Rozhodujte o krátkodobých úvěrových rámcích."],
    clients: ["Klienti a účty", "Rychlý dohled nad klientským portfoliem banky."],
    settings: ["Nastavení sazeb", "Spravujte úrokové sazby produktů dostupných klientům."],
  };

  async function saveInterestSettings(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setSaving(true);
    try {
      const updated = await updateInterestSettings({
        savingsRate: Number(form.get("savingsRate")), overdraftRate: Number(form.get("overdraftRate")),
        personalLoanRate: Number(form.get("personalLoanRate")), homeLoanRate: Number(form.get("homeLoanRate")),
      });
      setInterestSettings(updated);
      setError("");
    } catch (saveError) { setError(saveError instanceof Error ? saveError.message : "Sazby se nepodařilo uložit."); }
    finally { setSaving(false); }
  }

  return (
    <div className="admin-app">
      <aside className={`admin-sidebar ${menuOpen ? "is-open" : ""}`}>
        <div className="brand"><span className="brand-mark"><i /></span><span>Lístek<small>Manager</small></span></div>
        <button className="sidebar-close" onClick={() => setMenuOpen(false)} aria-label="Zavřít nabídku"><X size={20} /></button>
        <p className="nav-label">Pracovní prostor</p>
        <nav>
          {navigation.map(({ id, label, icon: Icon }) => (
            <button className={view === id ? "active" : ""} key={id} onClick={() => { setView(id); setSelected(null); setMenuOpen(false); }}>
              <Icon size={19} /><span>{label}</span>
              {id !== "clients" && <b>{id === "loans" ? dashboard?.pendingLoans : id === "overdrafts" ? dashboard?.pendingOverdrafts : (dashboard?.pendingLoans ?? 0) + (dashboard?.pendingOverdrafts ?? 0)}</b>}
            </button>
          ))}
        </nav>
        <div className="sidebar-foot"><ShieldCheck size={18} /><span>Zabezpečená administrace<small>Produkční prostředí</small></span></div>
      </aside>
      {menuOpen && <button className="menu-scrim" aria-label="Zavřít nabídku" onClick={() => setMenuOpen(false)} />}

      <main className="admin-main">
        <header className="topbar">
          <button className="menu-button" onClick={() => setMenuOpen(true)} aria-label="Otevřít nabídku"><Menu /></button>
          <div className="environment"><i /> Systémy v pořádku</div>
          <button className="icon-button" aria-label="Oznámení"><Bell size={19} /><span /></button>
          <div className="operator"><span>JP</span><div><strong>Jan Pokorný</strong><small>Úvěrový specialista</small></div></div>
        </header>

        <div className="admin-content">
          <section className="page-heading">
            <div><p>22. SRPNA 2026</p><h1>{titles[view][0]}</h1><span>{titles[view][1]}</span></div>
            {view !== "clients" && view !== "settings" && <div className="heading-actions"><div className="search"><Search size={18} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Hledat klienta nebo účet" /></div><label className="pending-filter"><input type="checkbox" checked={pendingOnly} onChange={(event) => setPendingOnly(event.target.checked)} /> Jen čekající</label></div>}
          </section>

          {error && <div className="notice">{error}</div>}
          {loading && <div className="loading">Načítám data banky...</div>}

          {!loading && view === "overview" && dashboard && <section className="metrics">
            <article><span className="metric-icon green"><Users size={21} /></span><div><small>Aktivní klienti</small><strong>{accounts.filter((account) => account.type === "CURRENT").length}</strong><p>Podle počtu běžných účtů</p></div></article>
            <article><span className="metric-icon amber"><FileClock size={21} /></span><div><small>Čeká na rozhodnutí</small><strong>{dashboard.pendingLoans + dashboard.pendingOverdrafts}</strong><p>{dashboard.pendingLoans} půjčky · {dashboard.pendingOverdrafts} kontokorenty</p></div></article>
            <article><span className="metric-icon blue"><Landmark size={21} /></span><div><small>Vklady klientů</small><strong>{money.format(dashboard.deposits)}</strong><p>Napříč všemi účty</p></div></article>
            <article><span className="metric-icon dark"><ClipboardCheck size={21} /></span><div><small>Dnes rozhodnuto</small><strong>{dashboard.decidedToday}</strong><p>Vyřízených žádostí</p></div></article>
          </section>}

          {view !== "clients" && view !== "settings" && !loading && <section className="work-layout">
            <div className="queue-panel">
              <div className="panel-heading"><div><p>{view === "overview" ? "PRIORITNÍ FRONTA" : "VŠECHNY ŽÁDOSTI"}</p><h2>{view === "loans" ? "Půjčky" : view === "overdrafts" ? "Kontokorenty" : "Žádosti k posouzení"}</h2></div><span>{visibleApplications.filter((item) => item.status === "PENDING").length} čeká</span></div>
              <div className="application-list">
                {visibleApplications.length === 0 && <div className="empty-state"><Check size={25} /><strong>Fronta je prázdná</strong><span>Žádné žádosti neodpovídají filtru.</span></div>}
                {visibleApplications.map((application) => (
                  <button key={application.id} className={selected?.id === application.id ? "selected" : ""} onClick={() => { setSelected(application); setNote(application.decisionNote ?? ""); }}>
                    <span className={`application-type ${application.category.toLowerCase()}`}>{application.category === "LOAN" ? <CircleDollarSign size={20} /> : <WalletCards size={20} />}</span>
                    <div><strong>{application.clientName}</strong><small>{application.category === "LOAN" ? (application.product === "PERSONAL" ? "Půjčka na cokoliv" : "Půjčka na bydlení") : "Kontokorent"} · {application.accountNumber}</small></div>
                    <div className="application-amount"><strong>{money.format(application.amount)}</strong><span className={`status ${application.status.toLowerCase()}`}>{application.status === "PENDING" ? "Čeká" : application.status === "APPROVED" ? "Schváleno" : "Zamítnuto"}</span></div>
                    <ChevronRight size={18} />
                  </button>
                ))}
              </div>
            </div>

            <aside className="detail-panel">
              {!selected ? <div className="detail-empty"><ClipboardCheck size={34} /><h2>Vyberte žádost</h2><p>V detailu uvidíte finanční údaje a provedete rozhodnutí.</p></div> : <>
                <div className="detail-head"><span className={`application-type ${selected.category.toLowerCase()}`}>{selected.category === "LOAN" ? <CircleDollarSign size={22} /> : <WalletCards size={22} />}</span><div><small>{selected.category === "LOAN" ? "ŽÁDOST O PŮJČKU" : "ŽÁDOST O KONTOKORENT"}</small><h2>{selected.clientName}</h2></div></div>
                <dl><div><dt>Požadovaná částka</dt><dd>{money.format(selected.amount)}</dd></div><div><dt>Účet</dt><dd>{selected.accountNumber}</dd></div>{selected.repaymentMonths && <div><dt>Splatnost</dt><dd>{selected.repaymentMonths} měsíců</dd></div>}{selected.monthlyPayment && <div><dt>Měsíční splátka</dt><dd>{money.format(selected.monthlyPayment)}</dd></div>}{selected.monthlyIncome && <div><dt>Měsíční příjem</dt><dd>{money.format(selected.monthlyIncome)}</dd></div>}<div><dt>Účel</dt><dd>{selected.purpose}</dd></div><div><dt>Podáno</dt><dd>{date.format(new Date(selected.createdAt))}</dd></div></dl>
                {selected.status === "PENDING" ? <div className="decision-box"><label>Poznámka k rozhodnutí<textarea value={note} onChange={(event) => setNote(event.target.value)} placeholder="Volitelná poznámka k rozhodnutí" maxLength={500} /></label><div><button className="reject" disabled={saving} onClick={() => decide("REJECTED")}><X size={17} /> Zamítnout</button><button className="approve" disabled={saving} onClick={() => decide("APPROVED")}><Check size={17} /> Schválit</button></div></div> : <div className={`decision-result ${selected.status.toLowerCase()}`}><strong>{selected.status === "APPROVED" ? "Žádost byla schválena" : "Žádost byla zamítnuta"}</strong><p>{selected.decisionNote || "Bez doplňující poznámky."}</p></div>}
              </>}
            </aside>
          </section>}

          {view === "clients" && !loading && <section className="clients-panel">
            <div className="panel-heading"><div><p>KLIENTSKÝ KMEN</p><h2>Účty a zůstatky</h2></div><span>{accounts.length} klientů</span></div>
            <div className="client-table"><div className="table-head"><span>Klient</span><span>Číslo účtu</span><span>Kontakt</span><span>Zůstatek</span></div>{accounts.map((account) => <article key={account.id}><span className="client-name"><i>{account.ownerName.split(" ").map((part) => part[0]).slice(0, 2).join("")}</i><strong>{account.ownerName}</strong></span><span>{account.accountNumber}</span><span>{account.email}</span><strong>{money.format(account.balance)}</strong></article>)}</div>
          </section>}

          {view === "settings" && !loading && interestSettings && <section className="settings-card"><div className="panel-heading"><div><p>PRODUKTOVÉ PODMÍNKY</p><h2>Úrokové sazby</h2></div><span>% p. a.</span></div><form onSubmit={saveInterestSettings} className="rate-form"><label>Spořicí účet<input name="savingsRate" type="number" min="0" step="0.001" defaultValue={interestSettings.savingsRate} /></label><label>Kontokorent<input name="overdraftRate" type="number" min="0" step="0.001" defaultValue={interestSettings.overdraftRate} /></label><label>Půjčka na cokoliv<input name="personalLoanRate" type="number" min="0" step="0.001" defaultValue={interestSettings.personalLoanRate} /></label><label>Půjčka na bydlení<input name="homeLoanRate" type="number" min="0" step="0.001" defaultValue={interestSettings.homeLoanRate} /></label><button className="primary-button submit-button" type="submit" disabled={saving}>{saving ? "Ukládám..." : "Uložit sazby"}</button></form></section>}
        </div>
      </main>
    </div>
  );
}
