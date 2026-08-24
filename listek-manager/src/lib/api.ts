export type Dashboard = {
  clients: number;
  pendingLoans: number;
  pendingOverdrafts: number;
  deposits: number;
  decidedToday: number;
};

export type Account = {
  id: string;
  ownerName: string;
  email: string;
  accountNumber: string;
  balance: number;
  currency: string;
  type: "CURRENT" | "SAVINGS";
  registrationStatus?: "PENDING" | "APPROVED" | "REJECTED";
};

export type AdminAuth = { token: string; username: string; mustChangePassword: boolean };
export type AdminUser = { username: string };

export type ApplicationStatus = "PENDING" | "APPROVED" | "REJECTED";

export type BankApplication = {
  id: string;
  category: "LOAN" | "OVERDRAFT";
  product: string;
  accountId: string;
  clientName: string;
  accountNumber: string;
  amount: number;
  repaymentMonths: number | null;
  monthlyIncome: number | null;
  monthlyPayment: number | null;
  purpose: string;
  status: ApplicationStatus;
  createdAt: string;
  decidedAt: string | null;
  decisionNote: string | null;
};

export type InterestSettings = {
  savingsRate: number;
  overdraftRate: number;
  personalLoanRate: number;
  homeLoanRate: number;
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = typeof window === "undefined" ? null : localStorage.getItem("listek-admin-session");
  const response = await fetch(`/api/admin${path}`, {
    ...init,
    cache: "no-store",
    headers: { "Content-Type": "application/json", ...(token ? { "X-Admin-Session": token } : {}), ...init?.headers },
  });
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "Požadavek se nepodařilo zpracovat.");
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export function adminLogin(input: { username: string; password: string }) { return request<AdminAuth>("/auth/login", { method: "POST", body: JSON.stringify(input) }); }
export function changeAdminPassword(password: string) { return request<void>("/auth/password", { method: "PATCH", body: JSON.stringify({ password }) }); }
export function createAdmin(input: { username: string; password: string }) { return request<AdminUser>("/users", { method: "POST", body: JSON.stringify(input) }); }
export function getPendingRegistrations() { return request<Account[]>("/registrations/pending"); }
export function decideRegistration(id: string, status: "APPROVED" | "REJECTED") { return request<Account>(`/registrations/${id}/decision`, { method: "PATCH", body: JSON.stringify({ status }) }); }

export function getDashboard() { return request<Dashboard>("/dashboard"); }
export function getAccounts() { return request<Account[]>("/accounts"); }
export function getLoans() { return request<BankApplication[]>("/loans"); }
export function getOverdrafts() { return request<BankApplication[]>("/overdrafts"); }
export function getInterestSettings() { return request<InterestSettings>("/settings/interest"); }
export function updateInterestSettings(input: InterestSettings) {
  return request<InterestSettings>("/settings/interest", { method: "PATCH", body: JSON.stringify(input) });
}

export function createOverdraft(input: { accountId: string; requestedLimit: number; monthlyIncome: number }) {
  return request<BankApplication>("/overdrafts", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function decideApplication(application: BankApplication, status: Exclude<ApplicationStatus, "PENDING">, note: string) {
  const resource = application.category === "LOAN" ? "loans" : "overdrafts";
  return request<BankApplication>(`/${resource}/${application.id}/decision`, {
    method: "PATCH",
    body: JSON.stringify({ status, note }),
  });
}