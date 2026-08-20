"use client";

import { ArrowDownLeft, ArrowRight, ArrowUpRight, Check, Plus, ShieldCheck, X } from "lucide-react";
import { FormEvent, useEffect, useRef, useState } from "react";
import BankShell from "../BankShell";
import { Account, BankTransaction, getAccounts, getTransactions, transferMoney } from "../../lib/api";

export default function PaymentsPage() {
  const [sent, setSent] = useState(false);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [error, setError] = useState("");
  const [fromAccountId, setFromAccountId] = useState("");
  const [toAccountNumber, setToAccountNumber] = useState("");
  const [transactions, setTransactions] = useState<BankTransaction[]>([]);
  const [selectedTransaction, setSelectedTransaction] = useState<BankTransaction | null>(null);
  const paymentFormRef = useRef<HTMLElement>(null);

  async function refreshTransactions(accountList: Account[]) {
    const accountTransactions = await Promise.all(accountList.map((account) => getTransactions(account.id)));
    setTransactions(accountTransactions.flat().sort((first, second) => new Date(second.createdAt).getTime() - new Date(first.createdAt).getTime()));
  }

  useEffect(() => {
    getAccounts().then(async (loadedAccounts) => {
      setAccounts(loadedAccounts);
      await refreshTransactions(loadedAccounts);
    }).catch(() => setError("Backend není dostupný. Platby nelze načíst ani odeslat."));
  }, []);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const selectedFromAccountId = String(form.get("fromAccountId"));
    const selectedToAccountNumber = String(form.get("toAccountNumber")).trim();
    const amount = Number(form.get("amount"));
    const description = String(form.get("description") || "Platba");
    if (!selectedFromAccountId || !selectedToAccountNumber) {
      setError("Vyplňte odesílající i cílový účet.");
      return;
    }
    try {
      await transferMoney({ fromAccountId: selectedFromAccountId, toAccountNumber: selectedToAccountNumber, amount, description });
      await refreshTransactions(accounts);
      setSent(true);
      setError("");
    } catch (transferError) {
      setError(transferError instanceof Error ? transferError.message : "Platbu se nepodařilo odeslat.");
    }
  }
  function accountForTransaction(transaction: BankTransaction) {
    return accounts.find((account) => account.id === transaction.accountId);
  }

  const transactionAccount = selectedTransaction ? accountForTransaction(selectedTransaction) : undefined;
  const counterpartyTransaction = selectedTransaction ? transactions.find((transaction) => transaction.id !== selectedTransaction.id && transaction.accountId !== selectedTransaction.accountId && transaction.description === selectedTransaction.description && transaction.amount === -selectedTransaction.amount) : undefined;
  const counterpartyAccount = counterpartyTransaction ? accountForTransaction(counterpartyTransaction) : undefined;
  const counterpartyNumber = selectedTransaction?.counterpartyAccountNumber ?? counterpartyAccount?.accountNumber;
  const transactionDate = selectedTransaction ? new Intl.DateTimeFormat("cs-CZ", { dateStyle: "long", timeStyle: "short" }).format(new Date(selectedTransaction.createdAt)) : "";

  function openNewPayment() {
    setSent(false);
    setError("");
    paymentFormRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    window.setTimeout(() => paymentFormRef.current?.querySelector("select")?.focus(), 350);
  }

  return <BankShell><div className="bank-content section-page">
    <div className="page-hero"><div><p className="date-label">Bezpečně a jednoduše</p><h1>Platby</h1><p className="page-lead">Pošlete peníze, nastavte trvalý příkaz nebo si prohlédněte šablony.</p></div><button className="pay-button" type="button" onClick={openNewPayment}><Plus size={19} /> Nová platba</button></div>
    <div className="payments-layout">
      <section className="payment-form-card" ref={paymentFormRef}><p className="modal-kicker">Tuzemská platba</p><h2>Komu posíláte?</h2>{error && <p className="api-notice">{error}</p>}{sent ? <div className="payment-success"><span><Check size={30} /></span><h2>Platba byla odeslána</h2><p>Backend převod zpracoval a zapsal odchozí pohyb.</p><button className="pay-button" type="button" onClick={openNewPayment}>Nová platba</button></div> : <form onSubmit={submit}><label>Odesílatel<select required name="fromAccountId" value={fromAccountId} onChange={(event) => { setFromAccountId(event.target.value); setError(""); }}><option value="" disabled>Vyberte účet</option>{accounts.map((account) => <option key={account.id} value={account.id}>{account.accountNumber} ({account.balance.toLocaleString("cs-CZ")} {account.currency})</option>)}</select></label><label>Cílový účet<input required name="toAccountNumber" value={toAccountNumber} onChange={(event) => { setToAccountNumber(event.target.value); setError(""); }} placeholder="Například 123456789 / 0100" /></label><div className="payment-fields"><label>Částka<input required name="amount" type="number" min="0.01" step="0.01" placeholder="0,00" /></label><label>Měna<select defaultValue="CZK"><option>CZK</option><option>EUR</option></select></label></div><label>Zpráva pro příjemce<input name="description" placeholder="Například oběd" /></label><button className="pay-button payment-submit" type="submit">Odeslat platbu <ArrowRight size={18} /></button><p className="secure-note"><ShieldCheck size={16} /> Platba se odešle přes zabezpečené REST API.</p></form>}</section>
      <aside className="quick-actions"><h2>Rychlé volby</h2><button><span><Plus size={18} /></span>Nový trvalý příkaz<ArrowRight size={16} /></button><button><span>↻</span>Opakovat platbu<ArrowRight size={16} /></button><button><span>⌁</span>Spravovat šablony<ArrowRight size={16} /></button></aside>
    </div>
    <section className="payment-history" aria-labelledby="payment-history-title">
      <div className="section-heading"><div><h2 id="payment-history-title">Seznam plateb</h2><p>Historie pohybů na vašich účtech</p></div><span className="payment-count">{transactions.length} plateb</span></div>
      <div className="payment-history-list">
        {transactions.map((transaction) => <button className="payment-history-row" key={transaction.id} onClick={() => setSelectedTransaction(transaction)}>
          <span className={`transaction-icon ${transaction.type === "CREDIT" ? "incoming" : ""}`}>{transaction.type === "CREDIT" ? <ArrowDownLeft size={19} /> : <ArrowUpRight size={19} />}</span>
          <span className="transaction-copy"><strong>{transaction.description}</strong><small>{transaction.type === "CREDIT" ? "Příchozí platba" : "Odchozí platba"} · {accountForTransaction(transaction)?.accountNumber ?? "Neznámý účet"}</small></span>
          <span className="transaction-date">{new Intl.DateTimeFormat("cs-CZ", { day: "numeric", month: "long" }).format(new Date(transaction.createdAt))}</span>
          <strong className={`amount ${transaction.type === "CREDIT" ? "incoming-amount" : ""}`}>{transaction.type === "CREDIT" ? "+" : "−"}{transaction.amount.toLocaleString("cs-CZ", { style: "currency", currency: accountForTransaction(transaction)?.currency ?? "CZK" })}</strong>
          <ArrowRight className="row-arrow" size={16} />
        </button>)}
        {transactions.length === 0 && <p className="bank-empty">Zatím nemáte žádné platby.</p>}
      </div>
    </section>
  </div>{selectedTransaction && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && setSelectedTransaction(null)}><section className="payment-modal transaction-detail-modal" role="dialog" aria-modal="true" aria-labelledby="transaction-detail-title"><button className="modal-close" onClick={() => setSelectedTransaction(null)} aria-label="Zavřít detail"><X size={21} /></button><p className="modal-kicker">Detail platby</p><h2 id="transaction-detail-title">{selectedTransaction.description}</h2><div className="transaction-detail-amount"><span className={selectedTransaction.type === "CREDIT" ? "incoming-amount" : ""}>{selectedTransaction.type === "CREDIT" ? "+" : "−"}{selectedTransaction.amount.toLocaleString("cs-CZ", { style: "currency", currency: transactionAccount?.currency ?? "CZK" })}</span><small>{selectedTransaction.type === "CREDIT" ? "Příchozí platba" : "Odchozí platba"}</small></div><dl className="transaction-detail-list"><div><dt>Datum a čas</dt><dd>{transactionDate}</dd></div><div><dt>Odchozí účet</dt><dd>{selectedTransaction.type === "DEBIT" ? transactionAccount?.accountNumber ?? "Neznámý účet" : counterpartyNumber ?? "Neuveden"}</dd></div><div><dt>Cílový účet</dt><dd>{selectedTransaction.type === "CREDIT" ? transactionAccount?.accountNumber ?? "Neznámý účet" : counterpartyNumber ?? "Neuveden"}</dd></div><div><dt>Majitel účtu</dt><dd>{transactionAccount?.ownerName ?? "Neznámý"}</dd></div><div><dt>Zpráva</dt><dd>{selectedTransaction.description}</dd></div><div><dt>ID transakce</dt><dd>{selectedTransaction.id}</dd></div></dl></section></div>}</BankShell>;
}
