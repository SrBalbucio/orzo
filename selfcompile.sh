#/bin/bash

# This will re-compile all white-listed classes with Orzo.
# This should run after unit and before integration tests, so that the integration tests can verify the self-compilation functionality.
# After Orzo is able to compile a large enough percentage of itself, we can move from a whitelist to a blacklist approach. 

# TODO: this is platform-dependent. Ideally, we should use Java or a Groovy script instead of Bash  
java -jar target/orzo-0.0.1-SNAPSHOT.jar $(cat whitelist.txt | tr '\n' ' ') -d target/classes -v