import { ArrowRight, Download, FileText, Search } from "lucide-react";
import BankShell from "../BankShell";

const documents = [
  ["Výpis z běžného účtu", "Srpen 2026", "PDF"],
  ["Výpis z běžného účtu", "Červenec 2026", "PDF"],
  ["Potvrzení o vedení účtu", "12. června 2026", "PDF"],
  ["Smlouva o běžném účtu", "3. ledna 2025", "PDF"],
];

export default function DocumentsPage() {
  return <BankShell><div className="bank-content section-page">
    <div className="page-hero"><div><p className="date-label">Vše důležité po ruce</p><h1>Dokumenty</h1><p className="page-lead">Výpisy, smlouvy a potvrzení bezpečně na jednom místě.</p></div></div>
    <section className="documents-panel"><div className="section-heading"><div><h2>Moje dokumenty</h2><p>Celkem 12 dokumentů</p></div><label className="transaction-search"><Search size={18} /><input placeholder="Hledat" aria-label="Hledat dokumenty" /></label></div><div className="document-list">{documents.map(([name, date, type]) => <button className="document-row" key={`${name}-${date}`}><span className="document-icon"><FileText size={19} /></span><span><strong>{name}</strong><small>{date}</small></span><b>{type}</b><Download size={17} /></button>)}</div><button className="all-transactions">Zobrazit všechny dokumenty <ArrowRight size={16} /></button></section>
  </div></BankShell>;
}
