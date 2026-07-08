# Ping Pong Process ${project.version} Description

The Ping Pong process allows testing connections to other (remote) DSF FHIR Endpoints. The following connections are tested:
- local DSF FHIR Server -> local BPE
- local BPE -> remote DSF FHIR Server
- remote DSF FHIR Server -> local DSF FHIR Server
- remote DSF FHIR Server -> remote BPE
- remote BPE -> local DSF FHIR Server
- local DSF FHIR Server -> remote DSF FHIR Server

The Ping Pong process's output paremters will link to a page which contains troubleshooting tips if any connection fails for known reasons. In order to test connections between DSF FHIR Servers, the process creates a Binary resource containing random data. Sending a reference to this resource to another DSF FHIR Server causes the server to verify the existence of the referenced resource on the requesting DSF FHIR Server, thus testing the connection. This allows the process to also be used to measure connection speeds as the Binary resource gets downloaded during execution and deleted before the process finishes. This means the process also tests the ability to transfer data, which can be helpful to make sure the DSF installation is properly set up in preparation for other process plugins involving data transfers.

Troubleshooting tips and network speeds can be found in the outputs of the completed process. They are named `potential-fix`, `download-speed-from-remote` and `upload-speed-to-remote`  respectively.

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://github.com/user-attachments/assets/7f037f1e-2498-4086-801c-27cccf7c9cb4">
  <source media="(prefers-color-scheme: light)" srcset="https://github.com/user-attachments/assets/21a3fa10-5ca5-4eee-bd1a-48e522e169a5">
  <img alt="Ping Pong Process Collaboration Diagram" src="https://github.com/user-attachments/assets/21a3fa10-5ca5-4eee-bd1a-48e522e169a5">
</picture>


### Autostart Process
The ping pong process plugin includes an autostart process which allows automatically starting the ping process on an interval.

# Ping-Pong: Ping
The **ping** process is used to ping other DSF FHIR Endpoints. It contains the following steps:
1. If requested, generate a Binary resource and store it on the local DSF FHIR Server
2. Send a ping message to all requested endpoints, containing a reference to the generated Binary resource
3. Wait for responses via pong messages
4. Download the Binary resource referenced in the pong message and measure network speed
5. Send a cleanup message to all endpoints
6. Delete the Binary resource generated in 1.
7. Store results and errors in the start task as output parameters

# Ping-Pong: Pong
The **pong** process is used to respond to incoming ping requests. It contains the following steps:
1. Download the Binary resource referenced in the ping message and measure network speed
2. If requested, generate a Binary resource and store it on the local DSF FHIR Server
3. Send a pong message to the requesting endpoint, containing a reference to the generated Binary resource, network speed and all errors that occured during execution so far
4. Wait for cleanup message
5. Delete the Binary resource generated in 2.
6. Store results and errors in the ping task as output parameters