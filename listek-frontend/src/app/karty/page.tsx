import { ArrowRight, CreditCard, LockKeyhole, Settings } from "lucide-react";
import BankShell from "../BankShell";

export default function CardsPage() {
  return <BankShell><div className="bank-content section-page">
    <div className="page-hero"><div><p className="date-label">Plaťte podle sebe</p><h1>Moje karty</h1><p className="page-lead">Karty máte pod kontrolou. Včetně limitů, bezpečnosti a nastavení.</p></div><button className="pay-button"><CreditCard size={18} /> Objednat kartu</button></div>
    <div className="cards-page-grid"><article className="card-large"><div className="card-chip" /><span className="card-brand">Lístek</span><p>••••&nbsp; ••••&nbsp; ••••&nbsp; 2841</p><div><span>JAN KRÁL</span><b>VISA</b></div></article><section className="card-info"><p className="card-status"><i /> Aktivní karta</p><h2>Visa Classic</h2><p>Platná do 08/29</p><div className="card-number-row"><span>Číslo karty</span><b>•••• 2841</b></div><button className="text-button">Zobrazit detaily <ArrowRight size={16} /></button></section></div>
    <div className="card-tools"><button><span><LockKeyhole size={20} /></span><strong>Dočasně zamknout</strong><small>Kartu můžete kdykoliv odemknout.</small></button><button><span><Settings size={20} /></span><strong>Limity a nastavení</strong><small>Upravte platby na internetu i výběry.</small></button></div>
  </div></BankShell>;
}
