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
};

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

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api/admin${path}`, {
    ...init,
    headers: { "Content-Type": "application/json", ...init?.headers },
  });
  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || "Požadavek se nepodařilo zpracovat.");
  }
  return response.json() as Promise<T>;
}

export function getDashboard() { return request<Dashboard>("/dashboard"); }
export function getAccounts() { return request<Account[]>("/accounts"); }
export function getLoans() { return request<BankApplication[]>("/loans"); }
export function getOverdrafts() { return request<BankApplication[]>("/overdrafts"); }

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