## Purpose
Universal Web Parser is a Software-as-a-Service (SaaS) platform that lets you parse any web page using Kotlin runtime scripts, executed at scheduled intervals. Each script has access to the standard Kotlin library and helper APIs.
The application:
- **Manages the browser lifecycle**, allowing scripts to focus purely on web scraping logic. Each script runs in an isolated browser context with its own persistent state and cookies, which are automatically saved and reloaded on every run. This allows scripts to behave more like real users—maintaining sessions, staying logged in, and avoiding detection as bots.
- **Uses Kotlin coroutines** for efficient, scalable concurrency, with an internal scheduler, task queue, and configurable workers for parallel execution.
A 5-minute cooldown period ensures that each script runs no more than once within that window. The exact execution time depends on worker availability and the number of scripts in the queue. Each script can store up to a preset number of unique results (default is 100).
---

## User Interface
The application includes a web-based interface, providing a dashboard where you can:
- Add, edit, and remove scripts
- View compilation status, errors, and scheduled execution times
- Enable, disable, or terminate running scripts
- Inspect execution results for each script

A standout feature of the user interface is its **real-time updates**:
- Running scripts are highlighted in green, and all metrics update in real time
- Web console displays real-time log updates from both the application and the scripts. Messages printed from scripts are prefixed with `SN>`, where `N` is the script ID.

## Script API
Each script is identified by a unique ID and has access to the following API:
- `println(Any)` – outputs any object to the web console.
- `page` – a ready-to-use Playwright Page instance.
- `store` – provides access to state. Scripts can use it as an internal store for sensitive information or as lightweight text storage. The store provides the following methods:
  - `load(): Map<String, String>`
  - `save(map: Map<String, String>)`
  - `remove(vararg keys: String)`
  - `canProcessAfter(url: String, hours: Int): Boolean` – Returns `true` the first time a URL is processed, then `false` until the specified number of hours has passed. Useful for throttling parsing. Internal storage for this method is not persisted.

The store can also be modified via REST endpoints by the admin.

Any value returned from the script is automatically converted to a JSON object; no manual conversion is needed.

## Users
The project was built for personal use, and no user registration is available. However, it defines two built-in users:
- `admin` – full access to all features
- `guest` – read-only access (except for scripts store, which is completely restricted)

Passwords for both users should be configured via environment variables on deployment.

## More
For more information check <a href="Readme.pdf" target="_blank">Readme.pdf</a>  
Live <a href="https://youtube.com" target="_blank">demonstration</a>
