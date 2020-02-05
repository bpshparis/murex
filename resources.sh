#!/bin/sh

output=vcap.json

echo '{}' | tee $output

services=$(ibmcloud resource service-instances | awk 'NR>6 {print "{\""$2"\":[{\"credentials\":null,\"name\":\""$1"\"}]}"}')

count=$(ibmcloud resource service-instances | awk -F'   ' 'NR>3 {count++} END {print count}') && echo $count " service-instances found."

instances=$(ibmcloud resource service-instances | awk -F'   ' 'NR>3 {print $1 ";;"}')

for i in $(seq 1 $count)
	do
		instance=$(echo $instances | awk -F ';;' '{print $'$i'  }');
		# service=$(echo $service | tr -d '[:space:]');
		instance=$(echo $instance | sed -e 's/^[[:space:]]*//');
		echo "Getting setting for service-instance "$instance;
		obj=$(ibmcloud resource service-instance "$instance" | awk -F ':' '/^ID:/ {print $6 ":" $7 ":" $9}');
		# echo $obj;

		service=$(echo $obj | cut -d':' -f1);
		region=$(echo $obj | cut -d':' -f2);
		id=$(echo $obj | cut -d':' -f3);

		region=$(ibmcloud resource service-instance "$instance" | awk -F ':' '/^ID:/ {print $7}');
		jq --argjson svc "{\"$id\":{\"credentials\":null,\"service\":\"$service\",\"region\":\"$region\",\"instance\":\"$instance\"}}" '. += $svc' $output | sponge $output;


	done

count=$(ibmcloud resource service-keys | awk -F'   ' 'NR>3 {count++} END {print count}') && echo $count " service-keys found."
keys=$(ibmcloud resource service-keys | awk -F'   ' 'NR>3 {print $1 ";;"}')

for i in $(seq 1 $count)
	do
		keyName=$(echo $keys | awk -F ';;' '{print $'$i'  }');
		# service=$(echo $service | tr -d '[:space:]');
		keyName=$(echo $keyName | sed -e 's/^[[:space:]]*//');
		echo "Getting setting for service-key "$keyName;
		obj=$(ibmcloud resource service-key "$keyName" --output JSON | jq -r .[0].id);
		instanceId=$(echo $obj | cut -d':' -f8);
		echo "instanceId=" $instanceId
		keyId=$(echo $obj | cut -d':' -f9);
		apikey=$(ibmcloud resource service-key "$keyName" --output JSON | jq -r .[0].credentials.apikey);
		apikey=$(echo $apikey | tr -d '[:space:]');
		url=$(ibmcloud resource service-key "$keyName" --output JSON | jq -r .[0].credentials.url);
		role=$(ibmcloud resource service-key "$keyName" --output JSON | jq -r .[0].credentials.iam_role_crn);
		role=$(echo $role | tr -d '[:space:]');

		cred='{"id": "'$keyId'", "name": "'$keyName'", "apikey": "'$apikey'", "url": "'$url'", "role": "'$role'"}';

		jq --argjson cred "$cred" 'if (.["'$instanceId'"]) then .["'$instanceId'"].credentials[.["'$instanceId'"].credentials| length] |= . + $cred else . end' $output | sponge $output;

	done

jq . $output

echo ""
echo "!!!! Resources available in " $(readlink -f $output) " !!!!"

# Sample usage:
# jq -r '.[] | select(.instance=="Visual Recognition-cv" and .credentials[1].role=="Writer") | .credentials[1].apikey + ":" + .credentials[1].role' $output


exit 0;

# Sample usage:
# jq -r '.[] | select(.instance=="Visual Recognition-cv" and .credentials[1].role=="Writer") | .credentials[1].apikey + ":" + .credentials[1].role' $output


exit 0;
