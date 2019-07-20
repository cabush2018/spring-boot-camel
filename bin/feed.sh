#!/bin/bash
commd="curl --header 'Content-Type: application/json' --noproxy localhost --request POST --data '$1'  http://localhost:9080/integration/$2"
echo 
echo ${commd}
echo 
`${commd}`
eval commd
