```
sudo docker pull baudelaine/wlp

NAME="slcdw"

sudo docker run -p 80:9080 -tdi --name $NAME -v $PWD:/app -v /home/fr054721:/home baudelaine/wlp

sudo docker start slcdw

sudo docker attach slcdw

ibmcloud update

ibmcloud config --check-version false
ibmcloud config --usage-stats-collect false

export USERID="sebastien.gautier@fr.ibm.com"

ibmcloud login -u ${USERID} --sso

ibmcloud iam api-key-create apikey1 -d "apikey1" --file /home/apikey1

ibmcloud login --apikey @home/apikey1 --no-region -g deafult

# Start Tone Analyzer

ibmcloud catalog service-marketplace | grep -i tone

ibmcloud catalog service tone-analyzer

ibmcloud resource service-instance-create ta tone-analyzer lite eu-de

ibmcloud resource service-key-create taKey Manager --instance-name ta

TA_APIKEY=$(ibmcloud resource service-key taKey --output json | jq -r .[].credentials.apikey) && echo $TA_APIKEY

TA_URL=$(ibmcloud resource service-key taKey --output json | jq -r .[].credentials.url) && echo $TA_URL

TA_VERSION="2017-09-21" && echo $TA_VERSION

TA_METHOD="/v3/tone" && echo $TA_METHOD

TA_REQUEST="$TA_METHOD/?version=$TA_VERSION" && echo $TA_REQUEST

TA_TEXT="On en a gros !" && echo $TA_TEXT

TA_INPUT_DATA="ta.req.json"

jq -n --arg value "$TA_TEXT" '{"text": $value}' | tee $TA_INPUT_DATA | jq .

TA_OUTPU_DATA="ta.resp.json"

LANG="fr"

curl -X POST -u 'apikey:'$TA_APIKEY -H 'Content-Type: application/json' -H 'Content-Language: '$LANG -H 'Accept-Language: '$LANG -d @$TA_INPUT_DATA $TA_URL$TA_REQUEST | tee $TA_OUTPU_DATA | jq .

# Start Natural Language Understanding

ibmcloud catalog service-marketplace | grep -i understand

ibmcloud catalog service natural-language-understanding

ibmcloud resource service-instance-create nlu natural-language-understanding free eu-de	

ibmcloud resource service-key-create nluKey Manager --instance-name nlu

export NLU_APIKEY=$(ibmcloud resource service-key nluKey --output json | jq -r .[].credentials.apikey) && echo $NLU_APIKEY
	
export NLU_URL=$(ibmcloud resource service-key nluKey --output json | jq -r .[].credentials.url) && echo $NLU_URL

NLU_VERSION="2020-08-01" && echo $NLU_VERSION

NLU_METHOD="/v1/analyze" && echo $NLU_METHOD

NLU_REQUEST="$NLU_METHOD/?version=$NLU_VERSION" && echo $NLU_REQUEST

export NLU_TEXT="J'aimerai avoir des nouvelles de ma commande passée il y a déjà 15 jours et que je n'ai toujours pas reçu." && echo $NLU_TEXT

NLU_FEATURES='{"sentiment": {}, "keywords": {}, "entities": {}}' && echo "$NLU_FEATURES" | jq .

NLU_INPUT_DATA="nlu.req.json"

jq -n --argjson features "$NLU_FEATURES" --arg text "$NLU_TEXT" '{"text": $text, "features": $features}' | tee $NLU_INPUT_DATA | jq .

NLU_OUTPUT_DATA="nlu.resp.json"

curl -X POST -u 'apikey:'$NLU_APIKEY -H 'Content-Type: application/json' -d @$NLU_INPUT_DATA $NLU_URL$NLU_REQUEST | tee $NLU_OUTPUT_DATA | jq .

# Start Visual Recognition

ibmcloud catalog service-marketplace | grep -i vis

ibmcloud catalog service watson-vision-combined
	
ibmcloud resource service-instance-create wvc watson-vision-combined lite eu-de	

ibmcloud resource service-key-create wvcKey Manager --instance-name wvc

WVC_APIKEY=$(ibmcloud resource service-key wvcKey --output json | jq -r .[].credentials.apikey) && echo $WVC_APIKEY

WVC_URL=$(ibmcloud resource service-key wvcKey --output json | jq -r .[].credentials.url) && echo $WVC_URL

WVC_VERSION="2018-03-19" && echo $WVC_VERSION

WVC_METHOD="/v3/classify" && echo $WVC_METHOD

WVC_REQUEST="$WVC_METHOD?version=$WVC_VERSION" && echo $WVC_REQUEST

IMG="/home/Pictures/pic0.jpg" && echo $IMG

WVC_OUTPUT_DATA="wvc.resp.json"

LANG="fr"

curl -X POST -u 'apikey:'$WVC_APIKEY -H 'Accept-Language: '$LANG -F 'images_file=@'$IMG $WVC_URL$WVC_REQUEST | tee $WVC_OUTPUT_DATA | jq .


```
