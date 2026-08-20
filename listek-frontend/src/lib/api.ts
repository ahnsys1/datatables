export type CurrencyCode = "CZK" | "EUR";

export type Account = {
  id: string;
  ownerName: string;
  accountNumber: string;
  balance: number;
  currency: CurrencyCode;
};

export type BankTransaction = {
  id: string;
  accountId: string;
  amount: number;
  type: "CREDIT" | "DEBIT";
  description: string;
  createdAt: string;
};

const apiBase = process.env.NEXT_PUBLIC_API_BASE_URL ?? "/api/backend";

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${apiBase}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...options?.headers },
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Backend odpověděl stavem ${response.status}`);
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export function getAccounts() {
  return request<Account[]>("/accounts", { cache: "no-store" });
}

export function getTransactions(accountId: string) {
  return request<BankTransaction[]>(`/accounts/${accountId}/transactions`, { cache: "no-store" });
}

export function transferMoney(input: {
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  description: string;
}) {
  return request<void>("/transfers", {
    method: "POST",
    body: JSON.stringify(input),
  });
}
