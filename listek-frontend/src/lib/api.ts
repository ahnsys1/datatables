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
  counterpartyAccountNumber?: string;
  createdAt: string;
};

export type BankCard = {
  id: string;
  accountId: string;
  holderName: string;
  cardType: string;
  lastFour: string;
  expirationDate: string;
  locked: boolean;
  paymentLimit: number;
  onlinePaymentLimit: number;
  withdrawalLimit: number;
  onlinePayments: boolean;
  inStorePayments: boolean;
  cashWithdrawals: boolean;
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

export function updateAccount(accountId: string, input: { ownerName: string }) {
  return request<Account>(`/accounts/${accountId}`, {
    method: "PATCH",
    body: JSON.stringify(input),
  });
}

export function getTransactions(accountId: string) {
  return request<BankTransaction[]>(`/accounts/${accountId}/transactions`, { cache: "no-store" });
}

export function getCards(accountId: string) {
  return request<BankCard[]>(`/accounts/${accountId}/cards`, { cache: "no-store" });
}

export function createCard(accountId: string) {
  return request<BankCard>(`/accounts/${accountId}/cards`, { method: "POST" });
}

export function updateCard(cardId: string, input: Omit<BankCard, "id" | "accountId" | "holderName" | "cardType" | "lastFour" | "expirationDate">) {
  return request<BankCard>(`/cards/${cardId}`, {
    method: "PATCH",
    body: JSON.stringify(input),
  });
}

export function transferMoney(input: {
  fromAccountId: string;
  toAccountNumber: string;
  amount: number;
  description: string;
}) {
  return request<void>("/transfers", {
    method: "POST",
    body: JSON.stringify(input),
  });
}
