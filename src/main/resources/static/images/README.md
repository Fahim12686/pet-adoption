# Images folder

This folder is where all site images live now (Phase 2.5). Because it's inside
`src/main/resources/static/`, everything here is part of the source tree —
it persists across app restarts and is version-controlled with the rest of
the project (unlike the old runtime `uploads/` folder, which was wiped
whenever the app restarted).

- `pets/` — pet photos, uploaded via the admin pet form (or drop files in manually)
- `store/` — store item photos, uploaded via the admin store form (or manually)
- `site/` — one-off static images used directly in templates, e.g.
  `site/adopter-form-hero.jpg` referenced from `adopter-form.html`

You can drop your own images into any of these folders and reference them
in templates as `/images/<folder>/<filename>` — Spring Boot serves this
whole `static/` directory automatically, no extra config needed.
