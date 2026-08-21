"use client";

import { ArrowRight, Landmark, ShieldCheck } from "lucide-react";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";
import { login, register } from "../../lib/api";
import { getSession, setSession } from "../../lib/session";

export default function LoginPage() {
  const router = useRouter();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (getSession()) router.replace("/");
  }, [router]);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError("");
    const form = new FormData(event.currentTarget);
    try {
      const account = mode === "login"
        ? await login({ email: String(form.get("email")), password: String(form.get("password")) })
        : await register({ ownerName: String(form.get("ownerName")), email: String(form.get("email")), address: String(form.get("address")), password: String(form.get("password")) });
      setSession(account);
      router.replace("/");
    } catch (submissionError) {
      setError(submissionError instanceof Error ? submissionError.message : "Požadavek se nepodařilo dokončit.");
    } finally {
      setSubmitting(false);
    }
  }

  function changeMode(nextMode: "login" | "register") {
    setMode(nextMode);
    setError("");
  }

  return <main className="auth-page"><section className="auth-brand"><div className="bank-logo"><span className="logo-mark"><span /></span><span>Lístek</span></div><div><p className="date-label">Internetové bankovnictví</p><h1>Vaše peníze.<br />Jednoduše.</h1><p>Bezpečný přístup k účtům, platbám a spoření na jednom místě.</p></div><span className="auth-security"><ShieldCheck size={18} /> Zabezpečené přihlášení</span></section><section className="auth-panel"><div className="auth-card"><span className="auth-icon"><Landmark size={24} /></span><h2>{mode === "login" ? "Přihlášení" : "Založení účtu"}</h2><p>{mode === "login" ? "Vítejte zpět v bance Lístek." : "Účet vám založíme během chvíle."}</p><div className="auth-tabs" role="tablist"><button className={mode === "login" ? "active" : ""} onClick={() => changeMode("login")} type="button">Přihlásit se</button><button className={mode === "register" ? "active" : ""} onClick={() => changeMode("register")} type="button">Registrovat</button></div>{error && <p className="api-notice">{error}</p>}<form onSubmit={submit}>{mode === "register" && <><label>Jméno a příjmení<input name="ownerName" required minLength={2} maxLength={120} autoComplete="name" /></label><label>Adresa<input name="address" required maxLength={240} autoComplete="street-address" /></label></>}<label>E-mail<input name="email" required type="email" autoComplete="email" /></label><label>Heslo<input name="password" required type="password" minLength={mode === "register" ? 8 : undefined} autoComplete={mode === "login" ? "current-password" : "new-password"} /></label><button className="pay-button payment-submit" type="submit" disabled={submitting}>{submitting ? "Zpracovávám..." : mode === "login" ? "Přihlásit se" : "Vytvořit účet"}<ArrowRight size={18} /></button></form></div></section></main>;
}