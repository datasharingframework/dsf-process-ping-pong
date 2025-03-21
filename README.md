# DSF Process Ping-Pong

In this repository you will find the ping-pong process for connection testing, which can be deployed on the [DSF](https://github.com/datasharingframework/dsf).

## Version 2.x release
Version 2.x of the ping-pong process (dubbed "Fat-Ping") includes the ability to test whether downloading FHIR resources from other DSF instances is possible. This also includes approximately measuring the network speed of resource downloads with larger download sizes (~100MB) returning more accurate results. It retains the ability to make connection tests without downloading resource like ping-pong 1.x. Documentation on configuration is available in the [wiki](https://github.com/datasharingframework/dsf-process-ping-pong/wiki).

## Development
Branching follows the git-flow model, for the latest development version see branch [develop](https://github.com/datasharingframework/dsf-process-ping-pong/tree/develop).

## License
All code is published under the [Apache-2.0 License](LICENSE).