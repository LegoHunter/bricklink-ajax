# BrickLink AJAX

`bricklink-ajax` is the internal BrickLink AJAX client used by LegoHunter services for catalog search and marketplace listing price discovery. It wraps BrickLink browser AJAX endpoints that are not part of the official BrickLink REST API.

The primary pricing use cases are:

- Resolve a public BrickLink item number, such as `6390-1`, to BrickLink's internal `idItem`.
- Fetch current BrickLink items for sale by internal `idItem`.
- Enforce a process-local safety rate limit across all BrickLink AJAX requests.

## Modules

```text
bricklink-ajax
├── bricklink-ajax-model
├── bricklink-ajax-client
└── bricklink-ajax-spring-boot-starter
```

### `bricklink-ajax-model`

DTOs and local filtering helpers for BrickLink AJAX responses.

Important model types:

- `SearchProductResult`
- `CatalogItemsForSaleResult`
- `Item`
- `ItemForSale`
- `Filters`

### `bricklink-ajax-client`

The Spring HTTP-interface client and public facade.

Important types:

- `BricklinkAjaxHttpClient`
- `DefaultBricklinkAjaxClient`
- `RateLimitedBricklinkAjaxHttpClient`
- `BricklinkAjaxRateLimiter`

The public facade is `com.bricklink.api.ajax.BricklinkAjaxClient`.

### `bricklink-ajax-spring-boot-starter`

Spring Boot auto-configuration for `BricklinkAjaxClient`.

Add this module to applications that want auto-configured access to the BrickLink AJAX client.

## Supported Endpoints

### Catalog Search

```text
GET /ajax/clone/search/searchproduct.ajax
```

Used to resolve BrickLink's public catalog item number to the internal `idItem`.

Example:

```text
https://www.bricklink.com/ajax/clone/search/searchproduct.ajax?q=6390-1&type=S
```

Relevant response fields:

```json
{
  "idItem": 4997,
  "typeItem": "S",
  "strItemNo": "6390-1",
  "strItemName": "Main Street"
}
```

Client helper:

```java
Optional<Item> item = bricklinkAjaxClient.findCatalogItem("6390-1", "S");
```

The helper only returns an item when all of these are true:

- `strItemNo` exactly matches the requested item number.
- `typeItem` exactly matches the requested item type.
- `idItem` is present.

If more than one exact match is returned, the client raises `BricklinkAjaxClientException`.

### Catalog Items For Sale

```text
GET /ajax/clone/catalogifs.ajax
```

Used to fetch current BrickLink listings for a catalog item by internal `idItem`.

Example:

```text
https://www.bricklink.com/ajax/clone/catalogifs.ajax?itemid=4997&cond=U&rpp=500&iconly=0
```

Client helper:

```java
CatalogItemsForSaleResult result =
        bricklinkAjaxClient.catalogItemsForSaleByInternalItemId(4997, "U", 500);
```

The client follows paged results by incrementing `pi` until all reported results are collected.

## Rate Limiting

BrickLink can ban external IP addresses when AJAX calls are made too aggressively. This client protects all AJAX HTTP calls with a configurable process-local minimum delay.

Default configuration:

```yaml
bricklink:
  ajax:
    uri: https://www.bricklink.com
    rate-limit:
      enabled: true
      minimum-delay-ms: 2000
    http-logging:
      enabled: false
```

The limiter is applied at the `BricklinkAjaxHttpClient` boundary, so every actual outbound AJAX request is protected, including extra pages fetched by `catalogItemsForSale`.

The limiter controls the minimum delay between request starts. If request one starts at `10:00:00`, request two cannot start before `10:00:02` with the default settings.

This rate limiter is a hard local safety guard. Higher-level systems, such as the Pricing Plane, should still schedule large crawls over long windows with blackout periods and jitter.

## Spring Boot Usage

Add the starter dependency:

```xml
<dependency>
    <groupId>com.bricklink.api</groupId>
    <artifactId>bricklink-ajax-spring-boot-starter</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

Inject the client:

```java
@RequiredArgsConstructor
@Service
class BricklinkCatalogIdHydrator {
    private final BricklinkAjaxClient bricklinkAjaxClient;

    Optional<Integer> internalItemId(String itemNumber) {
        return bricklinkAjaxClient.findCatalogItem(itemNumber, "S")
                .map(Item::getIdItem);
    }
}
```

## Pricing Plane Integration

Recommended pricing flow:

1. Load an active BrickLink marketplace listing.
2. Resolve its `external_catalog_item`.
3. If `external_unique_key` is missing, call `findCatalogItem`.
4. Store `idItem` in `external_unique_key`.
5. Use `catalogItemsForSaleByInternalItemId` during the price crawl.

Once `external_unique_key` is hydrated, it should be treated as durable catalog metadata and reused for future pricing crawls.

## Build

```text
mvn test
```
