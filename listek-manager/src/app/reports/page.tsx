"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import Reports from "./Reports";

import { Bell, CircleDollarSign, ClipboardCheck, LayoutDashboard, LogOut, ShieldCheck, Users, WalletCards } from "lucide-react";

export default function ReportsPage() {
  const router = useRouter();

  useEffect(() => {
    if (!localStorage.getItem("listek-admin-session")) router.replace("/");
  }, [router]);

  if (typeof window !== "undefined" && !localStorage.getItem("listek-admin-session")) return null;
  function signOut() {
    localStorage.removeItem("listek-admin-session");
    localStorage.removeItem("listek-admin-user");
    localStorage.removeItem("listek-admin-must-change");
    router.replace("/");
  }

  const navigation = [
    { href: "/overview", label: "Přehled", icon: LayoutDashboard },
    { href: "/loans", label: "Půjčky", icon: CircleDollarSign },
    { href: "/reports", label: "Reporty", icon: ClipboardCheck },
    { href: "/overdrafts", label: "Kontokorenty", icon: WalletCards },
    { href: "/clients", label: "Klienti", icon: Users },
    { href: "/settings", label: "Nastavení sazeb", icon: ClipboardCheck },
    { href: "/admins", label: "Administrátoři", icon: ShieldCheck },
  ];

  return (
    <div className="admin-app">
      <aside className="admin-sidebar">
        <div className="brand"><span className="brand-mark"><i /></span><span>Lístek<small>Manager</small></span></div>
        <p className="nav-label">Pracovní prostor</p>
        <nav>{navigation.map(({ href, label, icon: Icon }) => <button className={href === "/reports" ? "active" : ""} key={href} onClick={() => router.push(href)}><Icon size={19} /><span>{label}</span></button>)}</nav>
        <div className="sidebar-foot"><ShieldCheck size={18} /><span>Zabezpečená administrace<small>Produkční prostředí</small></span></div>
      </aside>
      <main className="admin-main">
        <header className="topbar"><div className="environment"><i /> Systémy v pořádku</div><button className="icon-button" aria-label="Oznámení"><Bell size={19} /><span /></button><button className="logout-button" type="button" onClick={signOut}><LogOut size={17} /> Odhlášení</button></header>
        <div className="admin-content">
          <section className="page-heading"><div><p>27. SRPNA 2026</p><h1>Reporty</h1><span>Sledujte schválení, splácení a zůstatky půjček.</span></div></section>
          <Reports />
        </div>
      </main>
    </div>
  );
}
