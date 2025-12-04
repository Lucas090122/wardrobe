# Vaatekaappi-sovellus — Käyttöopas

Tässä oppaassa kerrotaan, miten käytät **Vaatekaappi**-sovelluksen tärkeimpiä toimintoja, kuten:

- Jäsenten valinta ja hallinta  
- Vaatteiden lisääminen  
- NFC-pohjainen navigointi  
- AI-automaattinen täyttö  
- Tilastot ja siirtolohistoria  

Kuvat voidaan lisätä merkittyihin paikkoihin.

---

# 1. Sovelluksen yleiskuva

**Vaatekaappi** on perheille suunniteltu sovellus yhteisen vaatesäilytyksen hallintaan.  
Pääominaisuudet:

- Useiden jäsenten hallinta  
- NFC-pohjainen älysäilytys  
- AI-pohjainen vaatteiden ominaisuuksien automaattinen tunnistus  
- Tunnisteiden, luokkien, vuodenaikojen ja lämmöntasojen hallinta  
- Tilastot ja vaateanalytiikka  
- Vaatteiden siirtäminen jäsenten välillä  
- Sääperusteinen pukeutumissuositus  

---

# 2. Jäsenvalintanäkymä (aloitusnäkymä)

Kun avaat sovelluksen ensimmäisen kerran, näet **jäsenvalintanäkymän**.  
Jos jäseniä ei ole vielä luotu, näkymässä lukee esimerkiksi:

> Jäseniä ei vielä ole. Lisää jäsen ylhäältä!

Ylhäällä näkyy:

- Näkymän otsikko: **Koti**  
- Suuri painike **”Lisää uusi jäsen”**  
- Ylätason säätietopalkki (lämpötila + kuvake)  
- Vasemmassa yläkulmassa valikkokuvake (sivupalkki / drawer)  

<p align="center">
  <img src="https://github.com/user-attachments/assets/dde12f48-4545-4f06-bd54-0ee33f5da09d" width="240">
</p>

Tässä näkymässä voit:

- Lisätä uuden jäsenen  
- Valita aiemmin luodun jäsenen  
- Avata sivuvalikon ja siirtyä muihin osioihin  

---

## 2.1 Uuden jäsenen lisääminen

Paina **”Lisää uusi jäsen”** -painiketta.

Voit syöttää:

- Nimi  
- Sukupuoli  
- Ikä  
- (Valinnainen) Syntymäpäivä  

Jos ikä on alle 18, syntymäpäivä voidaan vaatia (riippuen validointisäännöistä).

Tallennuksen jälkeen:

- Uusi jäsen näkyy listassa  
- Voit valita hänet **aktiiviseksi jäseneksi**

<p align="center">
  <img src="https://github.com/user-attachments/assets/65bab165-95ec-4f48-b41d-ec1e40ec4b0a" width="240">
</p>

---

## 2.2 Aktiivisen jäsenen valinta

Jäsenvalintanäkymässä:

1. Napauta haluttua jäsentä  
2. Hänestä tulee **aktiivinen jäsen**  
3. Sovellus siirtyy kyseisen jäsenen vaatesivulle

Aktiivinen jäsen vaikuttaa:

- Etusivun / vaatelistan sisältöön  
- Tilastonäkymän tietoihin  
- Sääpohjaisiin asusuosituksiin  
- Siirtotoimintoon  

---

# 3. Sivupalkki (drawer)

Useimmissa näkymissä voit avata **sivupalkin** (drawer) vasemmasta yläkulmasta.

Sivupalkista on pikakuvakkeet seuraaviin:

- **Koti** – Aktiivisen jäsenen vaateinventaario  
- **Jäsen** – Jäsenvalinta ja hallinta  
- **Tilastot** – Tilastokaaviot  
- **Asetukset** – Asetusnäkymä  
- **Ylläpitotila** -kytkin  
- **AI-tila** -kytkin  
- **Tumma tila** -kytkin  

<p align="center">
  <img src="https://github.com/user-attachments/assets/0e296209-b6d1-417d-a240-86bae7db3e73" width="240">
</p>

### 3.1 Sivupalkin kohteet

#### Koti
Vie takaisin aktiivisen jäsenen vaateinventaarioon.

#### Jäsen
Avaa **jäsenvalintanäkymän**, jonka kautta voit vaihtaa nopeasti eri perheenjäseniin.

#### Tilastot
Avaa tilastonäkymän, jossa voit tarkastella esimerkiksi:

- Siirtolohistoriaa  
- Vaateinventaarion jakaumia  

#### Asetukset
Avaa asetussivun, jossa on mm. NFC-tarrojen yhdistäminen säilytyspaikkoihin.

---

### 3.2 Sivupalkin tilakytkimet

Seuraavat koko sovellusta koskevat tilat löytyvät **sivupalkista** (eivät Asetuksista):

#### Ylläpitotila
Mahdollistaa ylläpitäjätason toiminnot, kuten säilytyspaikan poistamisen, vaikka siihen olisi vielä liitetty tuotteita.  
Normaalitilassa tällaisia toimia ei voi tehdä.

#### AI-tila
Ottaa käyttöön tai poistaa käytöstä AI-pohjaisen automaattisen täytön.  
Kun AI-tila on pois päältä, vaatekuvia ei lähetetä analysoitavaksi.

#### Tumma tila
Vaihdat sovelluksen teeman vaalean ja tumman tilan välillä.

---

Kun olet valinnut jäsenen ja tutustunut sivupalkin valikkoihin, **Koti**-näkymä toimii sovelluksen päätyöskentelyalueena.

# 4. Koti / vaateinventaario

Jäsenen valinnan jälkeen sovellus siirtyy näkymään **Koti / vaateinventaario**.  
Tässä näkymässä voit:

- Tarkastella aktiivisen jäsenen kaikkia vaatteita  
- Selailla ja suodattaa listaa  
- Avata sivupalkin (drawer)  
- Lisätä uusia vaatteita  

<p align="center">
  <img src="https://github.com/user-attachments/assets/3c5f0f76-8cd6-49d3-97d3-9d39ec8ee8dd" width="240">
</p>

---

## 4.1 Vaatteen lisääminen

Etusivulla paina **”Lisää tuote”** -painiketta.

Lomakkeella voit täyttää:

- Kuvaus  
- Luokka (esim. Yläosa, Housut…)  
- Lämmöntaso  
- Tilaisuudet (esim. Arkinen, Koulu, Urheilu…)  
- Väri  
- Koko  
- Vuodenaika  
- Suosikki-merkintä  
- Tunnisteet  
- (Valinnainen) Säilytyspaikka  

<p align="center">
  <img src="https://github.com/user-attachments/assets/19ffe7e3-1244-42f5-be49-6728e23a926e" width="240">
</p>

---

## 4.2 AI-automaattinen täyttö (valinnainen)

Jos **AI-tila** on käytössä:

1. Lataa tai ota kuva vaatteesta  
2. Sovellus lähettää kuvan Googlen Gemini-palveluun  
3. Sovellus ehdottaa automaattisesti esimerkiksi:  
   - Luokkaa  
   - Lämmöntasoa  
   - Väriä  
   - Vuodenaikaa  

Ennen tallennusta käyttäjä voi muokata kaikkia kenttiä käsin.

---

# 5. NFC-älysäilytys

Sovellus tukee **fyysisten säilytyspaikkojen** (laatikot, kaapit, hyllyt) yhdistämistä **NFC-tarroihin**.

## 5.1 NFC-tarran yhdistäminen säilytyspaikkaan

Toimi näin:

1. Avaa sivupalkista **Asetukset**  
2. Valitse **”Lisää uusi NFC-tarra”** -toiminto  
3. Pidä uusi NFC-tarra puhelimen lähellä, jotta se havaitaan  
4. Valitse listasta säilytyspaikka, johon tarra liitetään  
5. Vahvista yhdistäminen  

<p align="center">
  <img src="https://github.com/user-attachments/assets/646d39c8-edd0-42f7-93f1-466358ff4c3c" width="240">
</p>

Yhdistämisen jälkeen tarra tallennetaan tietokantaan ja linkitetään kyseiseen säilytyspaikkaan.

---

## 5.2 Navigointi NFC-tarran avulla säilytyspaikkaan

Kun sovellus on etualalla ja **NFC-tila on lepotilassa (Idle)**:

1. Skannaa aiemmin yhdistetty NFC-tarra  
2. Sovellus etsii siihen liitetyn säilytyspaikan ID:n  
3. `MainViewModel` asettaa navigointipyynnön  
4. Sovellus avaa automaattisesti kyseisen **säilytyspaikkanäkymän**, jossa näkyvät paikan tuotteet  

---

# 6. Säilytyspaikkanäkymä

Säilytyspaikkanäkymässä näet:

- Säilytyspaikan nimen ja kuvauksen  
- Siellä säilytettyjen vaatteiden pienoiskuvat  

Voit:

- Tarkastella kaikkia siihen paikkaan liitettyjä vaatteita  

<p align="center">
  <img src="https://github.com/user-attachments/assets/bb2bbcd0-7380-4020-bb6f-c96f8846cfee" width="240">
</p>

---

# 7. Tuotteen tiedot ja siirtäminen jäseneltä toiselle

Vaatelistasta tai säilytyspaikkanäkymästä:

1. Napauta tuotetta → avautuu **Tuotteen tiedot** -näkymä  
2. Voit muokata tietoja, merkitä suosikiksi tai muuttaa säilytyspaikkaa  

Jos haluat **siirtää tuotteen toiselle jäsenelle**:

1. Avaa **Tuotteen tiedot**  
2. Paina **”Siirrä”**  
3. Valitse jäsen, jolle tuote siirretään  
4. Vahvista siirto  

Sovellus:

- Päivittää tuotteen omistajajäsenen  
- Tallentaa tapahtuman **siirtolohistoriaan**  
- Näyttää sen myöhemmin siirtolohistorianäkymässä  

<p align="center">
  <img src="https://github.com/user-attachments/assets/aefe220b-29bd-4da8-892d-42f5ba00b199" width="240">
  <img src="https://github.com/user-attachments/assets/971ea9bb-2f28-4381-91b3-08b3ce0b55ca" width="240">
</p>

---

# 8. Tilastonäkymä

**Tilastot**-näkymässä näet yhteenvedon aktiivisen jäsenen vaatekaapista, esimerkiksi:

- Luokkajakauma (yläosat, housut, kengät jne.)  
- Vuodenaikajakauma (kesä, talvi, kevät/syksy)  
- Jäsenkohtainen inventaario  

Kaaviot muodostetaan kirjautuneista tiedoista ja päivittyvät automaattisesti.

<p align="center">
  <img src="https://github.com/user-attachments/assets/06f4f55a-b3ac-49f1-a2c8-74b217ca460c" width="240">
  <img src="https://github.com/user-attachments/assets/a5ba5579-b1b6-4b05-9c7d-b05baeabe6ad" width="240">
  <img src="https://github.com/user-attachments/assets/bf463ee3-d017-42f9-8560-e73966091e97" width="240">
</p>

---

# 9. Asetusnäkymä

**Asetukset**-näkymässä on mm. seuraavat toiminnot:

- **Vaihda ylläpitäjän PIN** (jos ominaisuus on käytössä)  
- **Lisää uusi NFC-tarra** ja yhdistä se säilytyspaikkaan  

<p align="center">
  <img src="https://github.com/user-attachments/assets/2e1b4955-d5c1-4922-b1f2-b256f2101652" width="240">
</p>

---

# 10. Vinkkejä ja parhaita käytäntöjä

- Muista valita sovelluksen käynnistyessä oikea **aktiivinen jäsen**.  
- Kiinnitä NFC-tarrat laatikoihin, kaappeihin tai säilytyslaatikoihin, jotta löydät sisällön nopeasti.  
- AI-tila nopeuttaa huomattavasti suurten vaatemäärien lisäämistä.  
- Kun lapsi kasvaa, voit siirtää vaatteita nuoremmille sisaruksille **siirrä**-toiminnolla.  
- Tilastonäkymän avulla näet nopeasti, mitä vaatteita puuttuu ennen ostoksille lähtöä.  

---

# 11. Lisädokumentaatio

Lisätietoa sovelluksesta:

- **Projektin README:** [README.md](./README.md)  
- **Yksikkötestit ja kattavuus:** [TESTING.md](./TESTING.md)  

Tämä opas alkaa **jäsenvalintanäkymästä** ja esittelee Vaatekaappi-sovelluksen kaikki keskeiset toiminnot.
