---
type: C4 Component
title: Request field mapping
status: stable
groma:
  id: request-field-mapping
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/inbound/RequestFields.java
      symbol: RequestFields
description: Turns request values into domain values with field errors
---

Helper for controllers: creates domain value objects from request fields and turns a broken domain rule into a 400 error for that field (`InvalidFieldException`) instead of a 500.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [src/main/java/com/prokxi/dimples/inbound/RequestFields.java](../../../../../../src/main/java/com/prokxi/dimples/inbound/RequestFields.java) | [src/main/java/com/prokxi/dimples/exception/GlobalExceptionHandler.java](../../../../../../src/main/java/com/prokxi/dimples/exception/GlobalExceptionHandler.java) | Reports invalid fields | Java exception |
