import { ArrowRight, Plus, Target, TrendingUp } from "lucide-react";
import BankShell from "../BankShell";

export default function SavingsPage() {
  return <BankShell><div className="bank-content section-page">
    <div className="page-hero"><div><p className="date-label">Nechte peníze růst</p><h1>Spoření</h1><p className="page-lead">Vaše cíle, rezerva i úroky přehledně na jednom místě.</p></div><button className="pay-button"><Plus size={19} /> Nový cíl</button></div>
    <div className="savings-hero"><div className="savings-copy"><span className="savings-icon"><TrendingUp size={23} /></span><p className="modal-kicker">Spořicí účet</p><h2>84 200,00 Kč</h2><p>Úroková sazba <b>4,2 % p. a.</b></p><button className="text-button">Převést peníze <ArrowRight size={16} /></button></div><div className="goal-ring"><strong>84%</strong><span>finanční<br />rezerva</span></div></div>
    <div className="section-heading savings-heading"><h2>Vaše cíle</h2><button className="text-button">Spravovat cíle <ArrowRight size={16} /></button></div><div className="goal-list"><article><span><Target size={20} /></span><div><strong>Finanční rezerva</strong><small>100 000 Kč z 120 000 Kč</small><div className="saving-progress"><span /></div></div><b>84 %</b></article><article><span><Target size={20} /></span><div><strong>Dovolená v Portugalsku</strong><small>18 600 Kč z 35 000 Kč</small><div className="saving-progress orange-progress"><span /></div></div><b>53 %</b></article></div>
  </div></BankShell>;
}
