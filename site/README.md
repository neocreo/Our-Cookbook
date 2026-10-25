# site/ — opencookbook.online

Statisk webbplats för Our Open Cookbook, avsedd att publiceras på
opencookbook.online via Loopia.

## Filer

- `index.html` — landningssida (pitch, funktioner, kontakt)
- `sekretesspolicy.html` — sekretesspolicyn som krävs av Google Play/App Store
  (spegling av `_playstore/sekretesspolicy.md` — uppdatera båda vid ändring)
- `style.css` — delad stil, portad från appens Organic-tokens
- `icon-192.png` — app-ikonen (favicon/hero), kopia av `_playstore/grafik/icon-512.png` (nedskalad till 192 px)

## Publicera på Loopia

Publiceringen sker automatiskt via GitHub Actions
(`.github/workflows/deploy-site.yml`):

- Triggas vid push till `master` som ändrar något under `site/`
  (eller workflow-filen), samt manuellt via *Run workflow*.
- Speglar `site/` till FTP-värden med `lftp mirror --reverse --delete`,
  så filer som tagits bort ur repot raderas också på servern.
- FTP-uppgifterna ligger som repository secrets: `HOME_DIR` (FTP-adressen,
  t.ex. `opencookbook.online` eller `ftp://opencookbook.online`,
  ev. med sökväg), `HOME_USR` (användare) och `HOME_PASS` (lösenord).
  Namnen är medvetet opresenterande.
- Efter deploy kontrolleras att https://opencookbook.online/ svarar
  200 (steget är icke-blockerande tills HTTPS är aktiverat på domänen).

En gång per branch tillåts workflow-filen köras — efter att den mergats
till `master` körs första deployen automatiskt.

### Manuell reserv (om pipelinen inte kan köra)

1. Logga in på [loopia.se](https://www.loopia.se) → Loopia Customer zone.
2. Kontrollera att domänen opencookbook.online pekar på Loopias
   webbhotell (DNS: A-post till Loopias webbserver, eller Loopia DNS om
   domänen redan ligger hos Loopia).
3. Öppna **Webbhotell → Filhanterare** (eller anslut med FTP/SFTP).
4. Ladda upp allt innehåll i `site/` till domänens publiceringsmapp.
   Filerna ska ligga direkt i mappen, inte i en undermapp som `site/`.
5. Klart — [https://opencookbook.online](https://opencookbook.online)
   visar då `index.html`. Kontrollera även
   [https://opencookbook.online/sekretesspolicy.html](https://opencookbook.online/sekretesspolicy.html).

HTTPS: Loopia tillhandahåller ett gratis Let's Encrypt-certifikat — aktivera
det under domänens inställningar i Customer zone.

## Play Console

Ange sekretesspolicy-URL:n i Google Play Console (App content → Privacy
policy): `https://opencookbook.online/sekretesspolicy.html`
