"use client";

import { ArrowRight, Check, Plus, ShieldCheck } from "lucide-react";
import { FormEvent, useEffect, useState } from "react";
import BankShell from "../BankShell";
import { Account, getAccounts, transferMoney } from "../../lib/api";

export default function PaymentsPage() {
  const [sent, setSent] = useState(false);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [error, setError] = useState("");
  const [fromAccountId, setFromAccountId] = useState("");
  const [toAccountId, setToAccountId] = useState("");

  useEffect(() => {
    getAccounts().then(setAccounts).catch(() => setError("Backend není dostupný. Platbu nelze odeslat."));
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const selectedFromAccountId = String(form.get("fromAccountId"));
    const selectedToAccountId = String(form.get("toAccountId"));
    const amount = Number(form.get("amount"));
    const description = String(form.get("description") || "Platba");
    if (!selectedFromAccountId || !selectedToAccountId || accounts.length < 2) {
      setError("Pro převod potřebujete alespoň dva účty.");
      return;
    }
    if (selectedFromAccountId === selectedToAccountId) {
      setError("Odesílatel a příjemce musí být rozdílné účty.");
      return;
    }
    try {
      await transferMoney({ fromAccountId: selectedFromAccountId, toAccountId: selectedToAccountId, amount, description });
      setSent(true);
      setError("");
    } catch (transferError) {
      setError(transferError instanceof Error ? transferError.message : "Platbu se nepodařilo odeslat.");
    }
  }
  return <BankShell><div className="bank-content section-page">
    <div className="page-hero"><div><p className="date-label">Bezpečně a jednoduše</p><h1>Platby</h1><p className="page-lead">Pošlete peníze, nastavte trvalý příkaz nebo si prohlédněte šablony.</p></div><button className="pay-button"><Plus size={19} /> Nová platba</button></div>
    <div className="payments-layout">
      <section className="payment-form-card"><p className="modal-kicker">Tuzemská platba</p><h2>Komu posíláte?</h2>{error && <p className="api-notice">{error}</p>}{sent ? <div className="payment-success"><span><Check size={30} /></span><h2>Platba byla odeslána</h2><p>Backend převod zpracoval a zapsal oba pohyby.</p><button className="pay-button" onClick={() => setSent(false)}>Nová platba</button></div> : <form onSubmit={submit}><label>Odesílatel<select required name="fromAccountId" value={fromAccountId} onChange={(event) => { setFromAccountId(event.target.value); setToAccountId(""); setError(""); }}><option value="" disabled>Vyberte účet</option>{accounts.map((account) => <option key={account.id} value={account.id}>{account.accountNumber} ({account.balance.toLocaleString("cs-CZ")} {account.currency})</option>)}</select></label><label>Příjemce<select required name="toAccountId" value={toAccountId} onChange={(event) => { setToAccountId(event.target.value); setError(""); }}><option value="" disabled>Vyberte účet</option>{accounts.filter((account) => account.id !== fromAccountId).map((account) => <option key={account.id} value={account.id}>{account.accountNumber} ({account.ownerName})</option>)}</select></label><div className="payment-fields"><label>Částka<input required name="amount" type="number" min="0.01" step="0.01" placeholder="0,00" /></label><label>Měna<select defaultValue="CZK"><option>CZK</option><option>EUR</option></select></label></div><label>Zpráva pro příjemce<input name="description" placeholder="Například oběd" /></label><button className="pay-button payment-submit" type="submit">Odeslat platbu <ArrowRight size={18} /></button><p className="secure-note"><ShieldCheck size={16} /> Platba se odešle přes zabezpečené REST API.</p></form>}</section>
      <aside className="quick-actions"><h2>Rychlé volby</h2><button><span><Plus size={18} /></span>Nový trvalý příkaz<ArrowRight size={16} /></button><button><span>↻</span>Opakovat platbu<ArrowRight size={16} /></button><button><span>⌁</span>Spravovat šablony<ArrowRight size={16} /></button></aside>
    </div>
  </div></BankShell>;
}
