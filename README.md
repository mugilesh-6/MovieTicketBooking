# Show Scheduling Module

The module is Java 8 compatible and uses `ArrayList` for show persistence. It deliberately does not define `Movie`, `Theatre`, or `Screen` entities. `MovieCatalog` and `ScreenCatalog` are adapter contracts; implement them using the shared Member 1 repositories and map the existing objects to the small lookup projections `MovieDetails` and `ScreenDetails`.

## Member integration

- Construct one `ShowRepository` and one `ShowService` for the application. Pass the same Member 1-backed `MovieCatalog` and `ScreenCatalog` adapters used by the rest of the application.
- Implement `BookingGateway` using Member 3's booking service. `hasBookingsForShow` backs integration checks; `cancelBookingsForShow` must be idempotent and prevent new bookings for a show being cancelled. `theatreHasUpcomingBookings` should query bookings for future shows at that theatre.
- Implement `RefundGateway` with Member 4's refund service. `refundAllForShow` must be idempotent so cancellation can safely be retried after a downstream failure.
- Implement `SeatLockGateway` using Member 3's seat-lock service. Invalidate locks for the cancelled show; this operation must also be safe to repeat.
- Booking creation should obtain the price from `getCurrentSeatPrice` when a new booking is created, then store that price with the booking. Existing bookings must retain their booked price.
- Pass the authenticated role to the service. Show creation permits `ADMIN` and `THEATRE_MANAGER`; cancellation permits only `ADMIN`.
- Cancellation first marks a show `CANCELLATION_PENDING`, then cancels bookings, requests refunds, and invalidates locks. If an integration throws `ShowIntegrationException`, the pending status remains and an authorized caller can retry. A show is marked `CANCELLED` only when all three operations succeed.
- IDs are strings at this boundary. If the existing modules use another ID type, convert at the adapters; do not create parallel entity or repository instances.

The catalog projection classes are read-only scheduling inputs, not replacements for the Member 1 domain entities. `getMovieForShow` and `getScreenForShow` resolve the current shared-repository details through those adapters.

## Compile and run tests

From this directory, compile the module and its dependency-free test harness:

```powershell
$sources = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $sources
java -cp out com.movieticket.show.ShowServiceTest
```

The CLI is `com.movieticket.show.cli.ShowCli`. Create it with the application's shared `ShowService`, an input `Scanner`, an output `Appendable`, and the authenticated `ShowAccessRole`.
