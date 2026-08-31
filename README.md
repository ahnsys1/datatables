# datatables
## Spusteni
read -rsp 'Vault token: ' VAULT_TOKEN
export VAULT_TOKEN
echo
sh docker-compose-vault.sh up --build
unset VAULT_TOKEN