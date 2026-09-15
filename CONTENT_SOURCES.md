# AFC Prayer Content — Sources

Offline Android POC content for Apostolate for Family Consecration (AFC).  
Fetched: 2026-09-15 (America/New_York). Prayer bodies are transcribed from the URLs below; theological wording was not invented.

## Files

| File | Contents |
|------|----------|
| `afc_prayers.json` | 16 AFC daily / consecration / optional prayers |
| `divine_mercy.json` | Divine Mercy Chaplet bead steps + Hour of Great Mercy |
| `rosary.json` | Mysteries by day (incl. Luminous), bead template, traditional prayer texts, AFC after-Rosary set |
| `SOURCES.md` | This file |

## URLs used

### AFC (primary)

1. **https://afc.org/daily-prayers/**  
   Source for: AFC Morning Offering; Magnificat; Immaculate Conception Prayer for the Apostolate; St. Joseph Prayer for the Apostolate; Act of Consecration After Mass; St. Michael; Guardian Angel.  
   Obtained via WebFetch (full prayer bodies).

2. **https://afc.org/make-consecration/**  
   Source for: Prayer of Consecration to the Holy Family.  
   WebFetch timed out; content retrieved successfully via `curl` and HTML text extraction.

3. **https://afc.org/live-consecration/**  
   Source for: short All For daily renewal  
   (`All for the Sacred and Eucharistic Heart of Jesus, all through the Sorrowful and Immaculate Heart of Mary, all in union with St. Joseph.`).  
   WebFetch hit Cloudflare challenge; content retrieved successfully via `curl`.

4. **https://afc.org/wp-content/uploads/2025/07/323-127_DiscipleMembershipBooklet_2025_LoRes.pdf**  
   Disciple Membership Booklet (2025, LoRes). Extracted with `pdftotext`.  
   Source for: confirmation of Six Daily Prayers; All For Consecration Prayer after Rosary; Hail Holy Queen (after Rosary); St. Joseph Prayer after the Rosary; Act of Contrition (AFC form); Purgatory petition; Angelus; Regina Caeli; Act of Spiritual Communion; Cardinal Mercier Holy Spirit prayer; booklet’s short Chaplet of Divine Mercy outline.  
   PDF also lists (by title) the six daily prayers matching the website texts.

### Divine Mercy (Marian Fathers / USCCB)

5. **https://www.thedivinemercy.org/message/devotions/pray-the-chaplet**  
   Official Marian Fathers “How to Recite the Chaplet of The Divine Mercy.”  
   Source for: Sign of the Cross; optional openings (You expired…; O Blood and Water…; St. Faustina’s Prayer for Sinners); Our Father; Hail Mary; Apostles’ Creed; Eternal Father; “For the sake of His sorrowful Passion…”; Holy God…; optional closings (Eternal God…; Diary 1570).  
   Retrieved via `curl` (WebFetch timed out on this URL).

6. **https://www.thedivinemercy.org/message/devotions/hour**  
   Official Marian Fathers “Hour of Great Mercy.”  
   Source for: Diary 1320 quote; guidance on the 3 o’clock hour; brief prayer alternatives (“Jesus, Mercy”; “Jesus, for the sake of Your Sorrowful Passion…”).

7. **https://www.thedivinemercy.org/chj/prayers/cheat**  
   Consoler cheat sheet confirming Three O’clock Hour Prayer wording (You expired… / O Blood and Water…).

8. **https://www.usccb.org/prayers/how-pray-chaplet-divine-mercy**  
   Cross-check for Chaplet sequence and optional opening/closing wording (aligned with Marian Fathers).

### Rosary

9. **https://www.usccb.org/how-to-pray-the-rosary**  
   Source for: Rosary structure; Fatima Prayer wording; concluding Rosary prayer (“O God, whose Only Begotten Son…”); mystery sets and traditional days including Luminous (Thursdays); mystery fruits as listed by USCCB.

10. Traditional core prayers (Apostles’ Creed, Our Father, Hail Mary) for `rosary.json` taken from the Marian Fathers Chaplet page (#5 above), which prints the full traditional English texts. Glory Be uses the standard traditional English form commonly paired with the Rosary (see Gaps).

## Gaps / approximations

1. **Glory Be full text on USCCB how-to page** — USCCB names the Glory Be in the Rosary steps but does not print the full text on that page. `rosary.json` uses the traditional English form:  
   *“Glory be to the Father, and to the Son, and to the Holy Spirit, as it was in the beginning, is now, and ever shall be, world without end. Amen.”*  
   Marked with a `note` field in JSON. Not invented theological content; standard Catholic English.

2. **Minor textual variants between AFC website and PDF** — e.g. Magnificat punctuation/line breaks; “Will” vs “will”; “Patron Saints” capitalization; “the Apostolate” vs “the apostolate”; St. Joseph Prayer comma after “St. Joseph”. Website versions preferred for entries whose `sourceUrl` is the daily-prayers page; PDF preferred for after-Rosary / optional booklet-only prayers. Bodies are authentic in both cases.

3. **St. Faustina optional longer opening/closing** — Included in `divine_mercy.json` as optional (from Marian Fathers official Chaplet page). Core chaplet works without them.

4. **Individual USCCB prayer landing pages** (`/prayers/apostles-creed`, `/prayers/our-father`, etc.) — Fetch returned incomplete/non-prayer shells in this environment; full texts were taken from Marian Fathers Chaplet page and USCCB Rosary/Chaplet how-to pages instead.

5. **No Total Consecration (33-day de Montfort) full preparation text** — Not requested as a full extract; make-consecration page provided the Family Consecration prayer used here. Longer de Montfort preparation materials were not scraped into these files.

6. **Placeholder in Family Consecration prayer** — The blank `____________ family` is in the original AFC website text (fill-in for the family’s name), retained as-is.

## Method notes

- Tools: WebFetch, WebSearch, `curl`, `pdftotext` (poppler-utils).
- Cloudflare blocked some WebFetch attempts to afc.org; `curl` from the box succeeded for those pages.
- No prayer wording was composed or paraphrased for doctrine; where a source lacked a full printed text, the gap is noted above.
