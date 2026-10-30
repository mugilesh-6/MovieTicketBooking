# Movie Ticket Booking - Member 4

Payment, offer, and refund module for Java 17+. All persisted state is in-memory `ArrayList` repositories; payment processing is simulated and no gateway is contacted.

## Layers

- `cli`: `PaymentCli` interactive menu.
- `model`: payment, offer, breakdown, status, and refund types.
- `repository`: in-memory payment, offer, and refund stores.
- `service`: payment calculation/processing, offer validation, and cancellation/refund rules.
- `integration`: narrow ports and a `BookingDetails` projection used to connect to Member 3. This is not a replacement `Booking` or `User` model.

## Member 3 Integration

Create one shared `PaymentRepository`, `OfferRepository`, and `RefundRepository` in the application composition root. Implement `BookingServicePort` as an adapter around Member 3's `BookingService`: map its booking ID, user ID, show ID, show start time, and selected seat prices into `BookingDetails`; delegate confirmation, failure, cancellation, and show lookup to that service. Implement `SeatServicePort` by delegating `releaseLockedSeats` to Member 3's `SeatService`.

Pass those adapters to `PaymentService` and `RefundService`, sharing the same `PaymentRepository`. Pass the same `OfferService` to the payment service and CLI. Member 2's show-cancellation flow should call `RefundService.cancelShow(showId)`. Seat release should be idempotent in Member 3's `SeatService` because a show-cancellation request may be retried.

The host application starts the CLI with `new PaymentCli(paymentService, offerService, refundService, new Scanner(System.in)).run()`. Configure `PaymentSimulator` with a deterministic result for demos/tests; production-like integrations are intentionally outside this module.

Offer discounts are calculated against the pre-discount booking total. For user cancellation, the discount is allocated against the ticket component first, so the ticket refund is `max(ticket amount - offer discount, 0)`; convenience fee and GST are never refunded. Show cancellation refunds the complete charged amount.

## Compile and Test

From the project directory in PowerShell:

```powershell
$classes = Join-Path $env:TEMP 'movie-ticket-member4-classes'
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = Get-ChildItem -Path 'src' -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d $classes $sources
java -cp $classes com.movieticketbooking.member4.Member4ModuleTest
```

The executable test harness covers total calculation and booking confirmation, failed payment seat release, capped/one-time offers, user refund fee exclusion and duplicate prevention, and full show-cancellation refund.