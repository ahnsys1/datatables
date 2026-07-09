# Rezervace loděk v PHP

Jednoduchá aplikace v PHP + SQLite pro rezervaci 2 loděk po 30 minutách.

## Funkce
- registrace uživatele
- přihlášení uživatelským jménem a heslem
- administrátorské přihlášení `admin / admin`
- rezervace 2 loděk po 30minutových slotech
- změna půjčovacích hodin od zvoleného data
- automatické smazání rezervací mimo nový rozsah hodin
- vzhled inspirovaný přiloženým obrázkem

## Soubory
- `index.php` – hlavní přehled rezervací
- `login.php` – přihlášení
- `register.php` – registrace
- `admin.php` – administrace půjčovacích hodin
- `bootstrap.php` – SQLite připojení, inicializace DB a pomocné funkce
- `assets/style.css` – vzhled aplikace

## Spuštění
V adresáři `datatables/boats`:

```bash
php init_db.php
php -S localhost:8000
```

Pak otevřete:
- `http://localhost:8000/login.php`

## Poznámka
SQLite databáze se vytváří automaticky v souboru `storage.sqlite`.
