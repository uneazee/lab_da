# DevOps Exam Guide: Jenkins, Maven, STS + Bugzilla, Kubernetes

------------------------------------------------------------------------

## 🔴 1. Jenkins + GitHub

### Theory

Jenkins automates build and execution. GitHub stores code.

### Steps

1.  Create Java program
2.  Push to GitHub
3.  Install Jenkins
4.  Create Freestyle job
5.  Connect repo
6.  Add build commands
7.  Build and check output

### Pseudocode

START Create Java file Upload to GitHub Open Jenkins Create job Connect
repo Compile & run Check output END

### Troubleshooting

-   Check Git URL
-   Check Java installed
-   Check PATH

------------------------------------------------------------------------

## 🔵 2. Maven + JUnit

### Theory

Maven manages build and dependencies. JUnit is for testing.

### Steps

1.  Create project
2.  Add JUnit dependency
3.  Create class
4.  Create test
5.  Run mvn test

### Pseudocode

START Create project Add dependency Write method Write test Run mvn test
END

### Troubleshooting

-   mvn not found → install Maven
-   test not running → correct naming

------------------------------------------------------------------------

## 🟡 3. STS + Bugzilla

### Theory

STS builds Spring apps. Bugzilla tracks bugs.

### Steps

1.  Create Spring project
2.  Run app
3.  Open localhost
4.  File bug in Bugzilla

### Pseudocode

START Create project Run app Check output If error → file bug END

### Troubleshooting

-   Port issue
-   Browser issue

------------------------------------------------------------------------

## 🟢 4. Kubernetes

### Theory

Kubernetes manages containers and scaling.

### Steps

1.  kubectl get nodes
2.  Create deployment
3.  Scale pods
4.  Expose service
5.  Create YAML
6.  Apply config

### Pseudocode

START Check cluster Deploy app Scale Expose Apply YAML Verify END

### Troubleshooting

-   kubectl error
-   YAML indentation
-   Pod failure

------------------------------------------------------------------------

## 🔁 Quick Revision

Jenkins → CI/CD\
Maven → Build/Test\
STS → Development\
Bugzilla → Bug tracking\
Kubernetes → Deployment & scaling
