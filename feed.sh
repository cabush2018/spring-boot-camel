#!/bin/bash
curl --header \"Content-Type: application/json\" --noproxy localhost --request POST --data '$1' http://localhost:8080/integration/api/direct
