# Day 1 — Planning & Setup (08.09.2026)

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


# Day 2 — Development (09.09.2026)

Today was focused on rapidly turning the planning work from Day 1 into a working Android application.

The goal was to implement the complete core flow first and then improve the structure, UI, and reliability around it.

## Development Process

- Created GitHub issues for the main implementation tasks.
- Used the issues as a lightweight development checklist.
- Implemented the application incrementally instead of attempting to build everything at once.
- Regularly tested features after implementation to catch integration problems early.
- Kept commits focused on individual features and fixes where possible.

## Core Features Implemented

- Implemented the Splash Screen.
- Implemented the Home Screen.
- Added image selection from the device gallery.
- Added camera image capture.
- Implemented the Preview Screen.
- Added the ability to change or retake the selected image.
- Integrated the Pl@ntNet API.
- Implemented image upload using Retrofit.
- Parsed the Pl@ntNet API response.
- Implemented the Loading state during plant identification.
- Implemented the Result Screen.
- Displayed:
  - Common plant name
  - Scientific name
  - Confidence score
  - Family
  - Genus
- Added the ability to identify another plant after receiving a result.

## Architecture

The planned lightweight architecture was implemented during development.

The main flow became:

`Activity → Repository → PlantIdentifier → Pl@ntNet API`

This allowed the UI and API implementation to remain separated without introducing unnecessary complexity.

The `PlantIdentifier` abstraction was retained so that Pl@ntNet can be replaced by another identification provider in the future without requiring major changes to the application flow.

## Image Handling

One of the important implementation areas was preparing images correctly before sending them to the API.

The application handles images coming from both:

- Camera
- Gallery

Image processing was kept lightweight and focused on preparing images for reliable API upload while avoiding unnecessarily large requests.

## UI Implementation

The designs prepared during Day 1 were translated into Android XML layouts.

The objective was not to reproduce every design detail perfectly, but to maintain the same visual identity:

- Botanical green color palette
- Cream and light-green backgrounds
- Rounded UI components
- FloraScan branding
- Consistent spacing
- Simple navigation
- Clear primary actions

Reusable drawable resources were used where possible instead of duplicating styling across layouts.

## Testing

Testing was performed continuously during implementation.

### Manual Testing

The following flows were manually tested:

- Application launch
- Splash Screen navigation
- Gallery image selection
- Camera image capture
- Image preview
- Plant identification
- Loading state
- Successful API response
- Result rendering
- Repeated plant identification
- Navigation between screens

### Code-Level Testing

Basic code testing was also performed for areas where isolated behavior could be validated.

The main focus was ensuring that:

- API responses were parsed correctly
- Required values were extracted safely
- Null or missing data did not immediately break the UI
- Network failures could be handled without crashing the application

## Error Handling

Basic error handling was added for situations such as:

- Network connection problems
- API request failures
- Empty or invalid API responses
- Image selection problems
- Identification failures

The goal was to prevent technical API or networking errors from being directly exposed to the user.

## CI/CD

A lightweight CI/CD workflow was added to the GitHub repository.

The purpose was to automatically verify that the project could build successfully and to introduce a basic quality check for repository changes.

For the scope of this case study, I intentionally kept the pipeline simple rather than introducing unnecessary deployment infrastructure.

## Day 2 Reflection

The main objective of Day 2 was to move from planning to a complete working product as quickly as possible.

The architecture and UI preparation from Day 1 helped significantly because most major decisions had already been made before implementation started.

During development, I tried to maintain a balance between speed and structure.

Because this was a short case study, it would have been easy to place networking, API handling, and UI logic directly inside the Activities. Instead, I kept a small amount of separation through the repository and identification abstraction.

This added very little development overhead while making the code easier to understand, maintain, and extend.

The application was intentionally limited to the core functionality required by the case study.

I preferred finishing and testing a smaller, reliable feature set rather than creating additional features that could reduce the overall quality of the submission.


# Day 3 — Testing, Bug Fixes & Advertising Content (10.09.2026)

Day 3 was focused on stabilizing the application and completing the advertising portion of the case study.

## Application Testing

I performed another round of end-to-end testing across the main application flow:

`Launch → Camera / Gallery → Preview → Identify → Result`

The main focus was identifying problems that were less obvious during initial development.

This included:

- Navigation issues
- UI inconsistencies
- Image handling edge cases
- API response handling
- Loading behavior
- Repeated identification requests
- Error states

## Bug Fixes

Issues discovered during testing were fixed before spending additional time on optional features.

The priority remained application stability rather than increasing feature count.

I also reviewed the project for:

- Unnecessary debug code
- Hard-coded values
- Duplicate logic
- Inconsistent naming
- UI inconsistencies
- Potential null-related problems

## Advertising Videos

The second part of the case study required three advertising video concepts.

I created three different concepts rather than producing three variations of the same advertisement.

### Concept 1 — Discovery

Focused on curiosity.

The idea was to show a plant that the user sees but does not recognize, followed by FloraScan instantly identifying it.

### Concept 2 — Problem / Solution

Focused on a struggling plant and the uncertainty of not knowing what plant it is.

The advertisement presents FloraScan as the first step toward understanding the plant.

### Concept 3 — Speed & Discovery

Focused on rapid identification of multiple plants.

The objective was to make FloraScan feel fast, intelligent, and effortless.

## Generative AI Workflow

AI-assisted video generation was used to create the advertising content.

Instead of relying entirely on text prompts, I supplied the generated FloraScan application designs as visual references.

This helped maintain greater consistency between the actual application and the promotional material.

One issue encountered during video generation was AI-generated typography.

Generated text was sometimes visually incorrect or inconsistent.

Because of this, I changed the generation strategy:

- Use AI primarily for cinematic visuals and motion
- Preserve the application screenshots as visual references
- Avoid asking the video model to generate important new text
- Treat exact branding and typography as deterministic post-production elements

This was a useful reminder that generative models are strong creative tools, but not every element of a production workflow should be generated.

## Day 3 Reflection

Day 3 was mainly about refinement.

At this stage, adding new application features would have introduced unnecessary risk, so I focused instead on testing, bug fixing, documentation, and the required advertising deliverables.

The advertising work also reinforced an important engineering principle: AI should be used where it provides value, while deterministic tools should be preferred when consistency and exact output are required.

The overall project remained intentionally small, but I tried to approach it as a complete engineering delivery rather than only a functional prototype.
