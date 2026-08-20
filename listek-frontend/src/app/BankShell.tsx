"use client";

import {
  Bell, ChevronDown, CreditCard, FileText, HelpCircle, Home, Landmark,
  LogOut, Menu, Send, Settings, TrendingUp, X,
} from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { FormEvent, ReactNode, useEffect, useState } from "react";
import { Account, getAccounts, updateAccount } from "../lib/api";

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
  const [account, setAccount] = useState<Account | null>(null);
  const [profileOpen, setProfileOpen] = useState(false);
  const [profileName, setProfileName] = useState("");
  const [profileError, setProfileError] = useState("");
  const [profileSaving, setProfileSaving] = useState(false);

  useEffect(() => {
    getAccounts().then((accounts) => {
      if (accounts[0]) {
        setAccount(accounts[0]);
        setProfileName(accounts[0].ownerName);
      }
    }).catch(() => setProfileError("Profil se nepodařilo načíst."));
  }, []);

  function openProfile() {
    setProfileName(account?.ownerName ?? "");
    setProfileError("");
    setProfileOpen(true);
  }

  async function saveProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!account || !profileName.trim()) return;
    setProfileSaving(true);
    try {
      const updatedAccount = await updateAccount(account.id, { ownerName: profileName.trim() });
      setAccount(updatedAccount);
      setProfileName(updatedAccount.ownerName);
      setProfileOpen(false);
      setProfileError("");
    } catch (error) {
      setProfileError(error instanceof Error ? error.message : "Profil se nepodařilo uložit.");
    } finally {
      setProfileSaving(false);
    }
  }

  const displayName = account?.ownerName ?? "Načítám...";
  const initials = displayName.split(" ").filter(Boolean).slice(0, 2).map((part) => part[0]).join("").toUpperCase() || "JK";

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
            <button className="profile-button" onClick={openProfile}><span className="avatar">{initials}</span><span className="profile-name">{displayName}</span><ChevronDown size={16} /></button>
          </div>
        </header>
        {children}
      </main>
      {profileOpen && <div className="modal-backdrop" role="presentation" onMouseDown={(event) => event.target === event.currentTarget && setProfileOpen(false)}><section className="payment-modal profile-modal" role="dialog" aria-modal="true" aria-labelledby="profile-title"><button className="modal-close" onClick={() => setProfileOpen(false)} aria-label="Zavřít"><X size={21} /></button><p className="modal-kicker">Váš profil</p><h2 id="profile-title">Údaje majitele účtu</h2>{profileError && <p className="api-notice">{profileError}</p>}<form onSubmit={saveProfile}><label>Jméno a příjmení<input required minLength={2} maxLength={120} value={profileName} onChange={(event) => setProfileName(event.target.value)} /></label><button className="pay-button payment-submit" type="submit" disabled={profileSaving}>{profileSaving ? "Ukládám..." : "Uložit profil"}</button></form></section></div>}
    </div>
  );
}
