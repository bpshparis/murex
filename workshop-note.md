```
sudo docker pull baudelaine/wlp

NAME="slcdw"

sudo docker run -p 80:9080 -tdi --name $NAME -v $PWD:/app -v /home/fr054721:/home baudelaine/wlp

sudo docker start slcdw

sudo docker attach slcdw

geeqie ~/slcdw/appFlow.jpg

cd ~
WORKDIR="Webinar-FTPS"
[ -d "$WORDIR"] && rm -rf $WORKDIR
mkdir $WORKDIR && cd $WORKDIR

curl -fsSL https://clis.cloud.ibm.com/install/linux | sh

ibmcloud update
ibmcloud config --check-version false

export USERID="sebastien.gautier@fr.ibm.com"

ibmcloud login -u ${USERID} --sso

IC_APIKEY="ic_apikey"

ibmcloud iam api-key-create $IC_APIKEY -d "$IC_APIKEY" --file $IC_APIKEY

GROUP=$(ibmcloud resource groups --output json | jq -r .[0].name)
[ -z "$GROUP" ] && GROUP="default" || echo "group already set to" $GROUP 
echo $GROUP
REGION="eu-de" && echo $REGION

ibmcloud login --apikey @$IC_APIKEY -r $REGION -g $GROUP

# Start Tone Analyzer

ibmcloud catalog service-marketplace | grep -i tone

TA_SVC="tone-analyzer"

ibmcloud catalog service $TA_SVC

TA_NAME="ta"
TA_PLAN="lite"
TA_REGION="eu-de"

ibmcloud resource service-instance-create $TA_NAME $TA_SVC $TA_PLAN $TA_REGION

TA_KEYNAME="taKey"

ibmcloud resource service-key-create $TA_KEYNAME Manager --instance-name $TA_NAME

TA_APIKEY=$(ibmcloud resource service-key $TA_KEYNAME --output json | jq -r .[].credentials.apikey) && echo $TA_APIKEY

TA_URL=$(ibmcloud resource service-key taKey --output json | jq -r .[].credentials.url) && echo $TA_URL

TA_VERSION="2017-09-21" && echo $TA_VERSION

TA_METHOD="/v3/tone" && echo $TA_METHOD

TA_REQUEST="$TA_METHOD/?version=$TA_VERSION" && echo $TA_REQUEST

TA_TEXT="On en a gros !" && echo $TA_TEXT

TA_INPUT_DATA="ta.req.json"

jq -n --arg value "$TA_TEXT" '{"text": $value}' | tee $TA_INPUT_DATA | jq .

TA_OUTPUT_DATA="ta.resp.json"

TA_LANG="fr"

curl -X POST -u 'apikey:'$TA_APIKEY -H 'Content-Type: application/json' -H 'Content-Language: '$TA_LANG -H 'Accept-Language: '$TA_LANG -d @$TA_INPUT_DATA $TA_URL$TA_REQUEST | tee $TA_OUTPUT_DATA | jq .

# Start Natural Language Understanding

ibmcloud catalog service-marketplace | grep -i language

NLU_SVC="natural-language-understanding"

ibmcloud catalog service $NLU_SVC

NLU_NAME="nlu"
NLU_PLAN="free"
NLU_REGION="eu-de"

ibmcloud resource service-instance-create $NLU_NAME $NLU_SVC $NLU_PLAN $NLU_REGION	

NLU_KEYNAME="nluKey"

ibmcloud resource service-key-create $NLU_KEYNAME Manager --instance-name $NLU_NAME

NLU_APIKEY=$(ibmcloud resource service-key nluKey --output json | jq -r .[].credentials.apikey) && echo $NLU_APIKEY
	
NLU_URL=$(ibmcloud resource service-key nluKey --output json | jq -r .[].credentials.url) && echo $NLU_URL

NLU_VERSION="2020-08-01" && echo $NLU_VERSION

NLU_METHOD="/v1/analyze" && echo $NLU_METHOD

NLU_REQUEST="$NLU_METHOD/?version=$NLU_VERSION" && echo $NLU_REQUEST

NLU_TEXT="J'aimerai avoir des nouvelles de ma commande passée il y a déjà 15 jours et que je n'ai toujours pas reçu." && echo $NLU_TEXT

NLU_FEATURES='{"sentiment": {}, "keywords": {}, "entities": {}, "emotion": {}}' && echo "$NLU_FEATURES" | jq .

NLU_INPUT_DATA="nlu.req.json"

jq -n --argjson features "$NLU_FEATURES" --arg text "$NLU_TEXT" '{"text": $text, "features": $features}' | tee $NLU_INPUT_DATA | jq .

NLU_OUTPUT_DATA="nlu.resp.json"

curl -X POST -u 'apikey:'$NLU_APIKEY -H 'Content-Type: application/json' -d @$NLU_INPUT_DATA $NLU_URL$NLU_REQUEST | tee $NLU_OUTPUT_DATA | jq .

# Start Visual Recognition

ibmcloud catalog service-marketplace | grep -i vision

WVC_SVC="watson-vision-combined"

ibmcloud catalog service $WVC_SVC

WVC_NAME="wvc"
WVC_PLAN="lite"
WVC_REGION="eu-de"
	
ibmcloud resource service-instance-create $WVC_NAME $WVC_SVC $WVC_PLAN $WVC_REGION	

WVC_KEYNAME="wvcKey"

ibmcloud resource service-key-create $WVC_KEYNAME Manager --instance-name $WVC_NAME

WVC_APIKEY=$(ibmcloud resource service-key $WVC_KEYNAME --output json | jq -r .[].credentials.apikey) && echo $WVC_APIKEY

WVC_URL=$(ibmcloud resource service-key wvcKey --output json | jq -r .[].credentials.url) && echo $WVC_URL

WVC_VERSION="2018-03-19" && echo $WVC_VERSION

WVC_METHOD="/v3/classify" && echo $WVC_METHOD

WVC_REQUEST="$WVC_METHOD?version=$WVC_VERSION" && echo $WVC_REQUEST

IMG=~/"Pictures/pic1.jpg"

[ -f "$IMG" ] && ls -Alhtr $IMG || echo "ERROR: IMG does not exists" 

WVC_OUTPUT_DATA="wvc.resp.json"

WVC_LANG="fr"

curl -X POST -u 'apikey:'$WVC_APIKEY -H 'Accept-Language: '$WVC_LANG -F 'images_file=@'$IMG $WVC_URL$WVC_REQUEST | tee $WVC_OUTPUT_DATA | jq .

# Deploy on Cloud Foundry

ibmcloud target

ORG=$(ibmcloud account orgs --output JSON | jq -r .[0].OrgName) && echo $ORG
SPACE=$(ibmcloud account spaces -r $REGION -o $ORG --output JSON | jq -r .[0].name) && echo $SPACE

ibmcloud target --cf -r $REGION -o $ORG -s $SPACE -g $GROUP

ibmcloud resource service-alias-create ta --instance-name ta -g $GROUP -s $SPACE
ibmcloud resource service-alias-create nlu --instance-name nlu -g $GROUP -s $SPACE
ibmcloud resource service-alias-create wvc --instance-name wvc -g $GROUP -s $SPACE

# Get application code

git clone https://github.com/bpshparis/slcdw

cd slcdw

ibmcloud cf domains

vi manifest.yaml

ibmcloud cf p



```
