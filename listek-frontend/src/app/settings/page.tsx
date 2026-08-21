"use client";

import { FormEvent, useState } from "react";
import BankShell from "../BankShell";

export default function SettingsPage() {
  const [saved, setSaved] = useState(false);

  function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const values = Object.fromEntries(new FormData(event.currentTarget));
    localStorage.setItem("listek-settings", JSON.stringify(values));
    setSaved(true);
  }

  return <BankShell><div className="bank-content section-page"><div className="page-hero"><div><p className="date-label">Nastavení aplikace</p><h1>Nastavení</h1><p className="page-lead">Upravte způsob doručování upozornění a výchozí chování aplikace.</p></div></div><form className="settings-form" onSubmit={save}>{saved && <p className="success-notice">Nastavení bylo uloženo.</p>}<fieldset><legend>Upozornění</legend><label><span><strong>Pohyby na účtu</strong><small>Upozornit na každou příchozí a odchozí platbu.</small></span><input name="transactions" type="checkbox" defaultChecked /></label><label><span><strong>Platby kartou</strong><small>Potvrzení po každém použití karty.</small></span><input name="cardPayments" type="checkbox" defaultChecked /></label><label><span><strong>Měsíční výpis</strong><small>Poslat e-mail, až bude nový výpis připraven.</small></span><input name="statements" type="checkbox" defaultChecked /></label></fieldset><fieldset><legend>Zobrazení</legend><label><span><strong>Skrývat zůstatky</strong><small>Po otevření aplikace nezobrazovat částky.</small></span><input name="hideBalances" type="checkbox" /></label></fieldset><button className="pay-button" type="submit">Uložit nastavení</button></form></div></BankShell>;
}