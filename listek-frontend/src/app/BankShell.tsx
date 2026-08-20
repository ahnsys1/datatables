"use client";

import {
  Bell, ChevronDown, CreditCard, FileText, HelpCircle, Home, Landmark,
  LogOut, Menu, Send, Settings, TrendingUp, X,
} from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { ReactNode, useState } from "react";

const navigation = [
  { label: "Přehled", href: "/", icon: Home },
  { label: "Účty", href: "/ucty", icon: Landmark },
  { label: "Platby", href: "/platby", icon: Send },
  { label: "Karty", href: "/karty", icon: CreditCard },
  { label: "Spoření", href: "/sporeni", icon: TrendingUp },
  { label: "Dokumenty", href: "/dokumenty", icon: FileText },
];

export default function BankShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="bank-app">
      <aside className={`bank-sidebar ${menuOpen ? "is-open" : ""}`}>
        <div className="bank-logo" aria-label="Lístek banka"><span className="logo-mark"><span /></span><span>Lístek</span></div>
        <button className="sidebar-close" onClick={() => setMenuOpen(false)} aria-label="Zavřít nabídku"><X size={22} /></button>
        <nav className="bank-nav" aria-label="Hlavní navigace">
          {navigation.map(({ label, href, icon: Icon }) => (
            <Link className={pathname === href ? "active" : ""} href={href} key={label} onClick={() => setMenuOpen(false)}><Icon size={20} strokeWidth={1.8} /><span>{label}</span></Link>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <button><HelpCircle size={20} /><span>Pomoc a kontakt</span></button>
          <button><Settings size={20} /><span>Nastavení</span></button>
          <button><LogOut size={20} /><span>Odhlásit se</span></button>
        </div>
      </aside>
      {menuOpen && <button className="menu-scrim" onClick={() => setMenuOpen(false)} aria-label="Zavřít nabídku" />}
      <main className="bank-main">
        <header className="bank-header">
          <button className="mobile-menu" onClick={() => setMenuOpen(true)} aria-label="Otevřít nabídku"><Menu /></button>
          <div className="mobile-logo">Lístek</div>
          <div className="header-actions">
            <button className="icon-button notification" aria-label="Oznámení"><Bell size={20} /><span /></button>
            <button className="profile-button"><span className="avatar">JK</span><span className="profile-name">Jan Král</span><ChevronDown size={16} /></button>
          </div>
        </header>
        {children}
      </main>
    </div>
  );
}
