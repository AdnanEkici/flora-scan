# Day 1 — Planning & Setup

Today was focused on defining the direction of the project before starting implementation.

- Created the initial application architecture.
- Prepared a two-day implementation roadmap.
- Identified the main modules required for the application.
- Selected Pl@ntNet as the plant identification API.
- Obtained the Pl@ntNet API key required for development and testing.
- Selected the development stack:
  - Android Studio
  - Java
  - XML
  - Retrofit
  - Gson
  - Glide
- Created the GitHub repository for FloraScan.
- Completed the initial application design and defined the main user flow and screens.
- Finalized the UI design for:
  - Splash Screen
  - Home Screen
  - Preview Screen
  - Loading Screen
  - Result Screen
- Created the main visual assets required for development.
- Created the FloraScan logo.
- Defined the application color scheme.
- Created reusable background assets for the main screens.
- Prepared the visual direction to be implemented directly with Android XML layouts.
- Defined a lightweight architecture suitable for the short development timeline.
- Added a repository layer to separate UI and data-access logic.
- Added a `PlantIdentifier` abstraction so the identification provider can be replaced later if needed.
- Decided to keep the current implementation simple and avoid unnecessary backend infrastructure or over-engineering.

## Day 1 Reflection

The main objective was to reduce uncertainty before implementation.

By the end of the planning phase, the application architecture, development roadmap, UI flow, visual design, assets, logo, color scheme, API provider, and API credentials were ready.

This means the project is now prepared to move directly into implementation without spending development time on major design or architecture decisions.

Since the development window is short, the project will focus on delivering a reliable core feature rather than adding unnecessary complexity.

The application design and architecture are intentionally simple, but a few extension points were included to demonstrate how the project could evolve into a larger production system.

## P.S.

Most of the decisions documented here are not fixed and may change during development.

The architecture, UI details, module responsibilities, API usage, and implementation approach can be adjusted if testing or development reveals a simpler, more reliable, or more suitable solution.
