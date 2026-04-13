public class Main{
    public static void main(String[] args){
        System.out.println("Name: Anisha");
    }
}

"""


Application Development and Deployment Architecture Submission 4.pdf
PDF

Application Development and Deployment Architecture Submission 3.pdf
PDF

Application Development and Deployment Architecture Submission 2.pdf
PDF

Application Development and Deployment Architecture Submission 1.pdf
PDF

from these extract the following and give a step by step guide for exam. write about every little step and code required possible so i can easily follow, include websites  (github, bugzilla etc)

Jenkins, Maven, Bugzilla with STS, and Kubernetes (all four programs).

Great — I’ve gone through all 4 of your PDFs and combined them into a clear, exam-ready step-by-step guide for:

Jenkins + GitHub

Maven

Bugzilla with STS

Kubernetes

I’ll explain every step + commands + code + websites so you can reproduce it easily in exams 💯

🔴 1. JENKINS + GITHUB (CI PIPELINE)
🔗 Websites
GitHub: https://github.com

Jenkins: https://www.jenkins.io

Download Jenkins: https://www.jenkins.io/download

✅ Step 1: Create Java Program
Create file: Main.java

public class Main {
    public static void main(String[] args) {
        System.out.println("Name: Your Name");
        System.out.println("Register Number: 22XXXX");
        System.out.println("Jenkins–GitHub Integration Successful");
    }
}
✅ Step 2: Push to GitHub
Commands:
git init
git add .
git commit -m "First commit"
git branch -M main
git remote add origin https://github.com/your-username/DA1_22XXXX.git
git push -u origin main
👉 Make at least 2 commits

✅ Step 3: Install & Start Jenkins
Install Jenkins

Open: http://localhost:8080

Unlock using password

Install suggested plugins

✅ Step 4: Create Jenkins Job
Click New Item

Choose Freestyle Project

Name: DA1_22XXXX

✅ Step 5: Connect GitHub
Go to Source Code Management

Select Git

Paste your GitHub repo URL

✅ Step 6: Add Build Step
In Build → Execute Windows Batch Command (or Shell):

javac Main.java
java Main
✅ Step 7: Build Project
Click Build Now

Go to Console Output

✔ Expected output:

Name: Your Name
Register Number: 22XXXX
Jenkins–GitHub Integration Successful
🔵 2. MAVEN PROJECT + JUNIT
🔗 Website
Maven: https://maven.apache.org

✅ Step 1: Create Maven Project
mvn archetype:generate
Or use IDE (STS / IntelliJ)

✅ Step 2: pom.xml (IMPORTANT)
Add JUnit 5 dependency:

<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.9.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
✅ Step 3: Create Class
OrderService.java
public class OrderService {
    public double calculateTotal(double price, int quantity) {
        return price * quantity;
    }
}
✅ Step 4: Test Class
OrderServiceTest.java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {

    @Test
    void testCalculateTotal() {
        OrderService service = new OrderService();
        double result = service.calculateTotal(100, 2);
        assertEquals(200, result);
    }
}
✅ Step 5: Run Maven Commands
mvn compile
mvn test
mvn clean test
mvn test -X
mvn test -Dtest=OrderServiceTest
mvn install -DskipTests
✅ Step 6: Project Structure
project/
 ├── src/
 │   ├── main/java/
 │   │   └── OrderService.java
 │   └── test/java/
 │       └── OrderServiceTest.java
 └── pom.xml
🟡 3. STS (SPRING TOOL SUITE) + BUGZILLA
🔗 Websites
STS Download: https://spring.io/tools

Bugzilla: https://bugzilla.mozilla.org

✅ Step 1: Create Spring Boot Project (STS)
File → New → Spring Starter Project

Fill:

Name: demo

Type: Maven

Java: 21

Packaging: Jar

Add dependency: Spring Web

Click Finish

✅ Step 2: Run Project
Right click project

Run As → Spring Boot App

✔ Output in console:

Tomcat started on port 8080
✅ Step 3: Open in Browser
http://localhost:8080
✅ Step 4: Bugzilla Account
Go to Bugzilla

Click Create Account

Verify email & login

✅ Step 5: File a Bug
Fill:

Product: WebTools

Summary: Home page not loading properly

Description:

Application home page is not displaying expected output
Steps to Reproduce:

1. Open browser
2. Enter localhost:8080
3. Observe output
Expected Result:

Correct message
Actual Result:

Incorrect output
Severity: Normal

Click Submit Bug

✔ Bug ID generated

🟢 4. KUBERNETES (VERY IMPORTANT FOR EXAM)
🔗 Websites
Kubernetes: https://kubernetes.io

Playground: https://killercoda.com

✅ Step 1: Check Cluster
kubectl get nodes
✅ Step 2: Create Deployment
kubectl create deployment webapp --image=nginx
✅ Step 3: Check Pods
kubectl get pods
✅ Step 4: Scale Deployment
kubectl scale deployment webapp --replicas=3
Check:

kubectl get pods
✅ Step 5: Expose Service
kubectl expose deployment webapp --type=NodePort --port=80
Check:

kubectl get services
✅ Step 6: Create YAML File
Create file: limited-pod.yaml

apiVersion: v1
kind: Pod
metadata:
  name: limited-pod
spec:
  containers:
  - name: nginx
    image: nginx
    resources:
      limits:
        memory: "128Mi"
        cpu: "500m"
✅ Step 7: Apply YAML
kubectl apply -f limited-pod.yaml
✅ Step 8: Verify Pod
kubectl get pods
kubectl describe pod limited-pod
✔ Check:

Limits: cpu: 500m, memory: 128Mi
👉 This is controlled by CGroup

✅ Step 9: Show All Resources
kubectl get all
🧠 EXAM TIPS (VERY IMPORTANT)
Always write:

Commands

Output

Explanation (1 line each step)

Mention tools:

Jenkins → CI/CD

Maven → Build tool

Bugzilla → Bug tracking

Kubernetes → Container orchestration

🚀 FINAL SUMMARY (WRITE IN EXAM)
Jenkins integrates with GitHub to automate builds

Maven manages dependencies and testing

STS helps build Spring Boot apps

Bugzilla tracks bugs with IDs

Kubernetes deploys, scales, and manages containers

If you want, I can also:
✅ Convert this into perfect 10-mark/15-mark answer format
✅ Or give viva questions + answers
✅ Or make diagrams (CI/CD, Kubernetes architecture)


give written process + pseudocode + steps incase i forget code, or if something isnt working

Got it — this is exactly what you need for exams and real troubleshooting 👍
I’ll give you for each tool:

✔ Written process (theory you can write)
✔ Step-by-step actions
✔ Pseudocode (memory-friendly)
✔ “If not working” fixes

🔴 1. JENKINS + GITHUB
✍️ Written Process (Exam Theory)
Jenkins is a CI/CD tool used to automate build and execution of code. GitHub is used to store source code. Jenkins pulls code from GitHub, compiles it, and runs it automatically whenever changes are made.

🔢 Steps
Create Java program

Push code to GitHub repository

Install Jenkins and open dashboard

Create Freestyle project

Connect GitHub repo

Add build commands

Run build and check output

🧠 Pseudocode (Easy to Remember)
START
Create Java file
Upload file to GitHub

Open Jenkins
Create new job

Connect GitHub repo
Add build command:
    compile code
    run code

Click Build
Check console output
END
❗ If Something Doesn’t Work
❌ GitHub not connecting
✔ Check repo URL (no typo)
✔ Use HTTPS not SSH

❌ Build fails
✔ Check Java installed:

java -version
❌ “javac not found”
✔ Add Java to PATH

❌ No output
✔ Check console logs in Jenkins

🔵 2. MAVEN + JUNIT
✍️ Written Process
Maven is used for project build and dependency management. JUnit is used for testing Java applications. Maven compiles code, runs tests, and manages libraries automatically.

🔢 Steps
Create Maven project

Add JUnit dependency in pom.xml

Create Java class

Write test class

Run tests using Maven

🧠 Pseudocode
START
Create Maven project

Add dependency (JUnit)

Create class:
    method = calculateTotal

Create test:
    call method
    check expected output

Run command:
    mvn test

If test passes → SUCCESS
Else → FIX code
END
❗ If Something Doesn’t Work
❌ Maven not found
✔ Install Maven
✔ Check:

mvn -version
❌ Dependency not downloading
✔ Check internet
✔ Run:

mvn clean install
❌ Test not running
✔ Ensure file name ends with Test.java

❌ Compilation error
✔ Check package names

🟡 3. STS + BUGZILLA
✍️ Written Process
STS (Spring Tool Suite) is used to develop Spring Boot applications. Bugzilla is a bug tracking system used to report and manage software issues.

🔢 Steps (STS)
Open STS

Create Spring Starter Project

Add dependencies (Spring Web)

Run application

Open browser (localhost:8080)

🔢 Steps (Bugzilla)
Create account

Login

Click “File a Bug”

Enter details

Submit bug

Bug ID generated

🧠 Pseudocode
START
Open STS
Create project

Add dependency
Run project

Open browser
Check output

IF output wrong:
    Open Bugzilla
    Login
    File bug:
        summary
        steps
        expected
        actual

Submit
Bug ID generated
END
❗ If Something Doesn’t Work
❌ App not running
✔ Check port 8080 free

❌ White label error
✔ Add controller or endpoint

❌ Browser not loading
✔ Check:

http://localhost:8080
❌ Bugzilla not sending mail
✔ Check spam folder

🟢 4. KUBERNETES
✍️ Written Process
Kubernetes is used to deploy and manage containerized applications. It allows scaling, load balancing, and resource control using CGroup.

🔢 Steps
Start Kubernetes cluster

Create deployment using nginx

Check pods

Scale deployment

Expose service

Create YAML file for limits

Apply YAML

Verify resources

🧠 Pseudocode
START
Check cluster

Create deployment (nginx)

Check pods running

Scale deployment to 3

Expose service

Create YAML:
    set CPU + memory limits

Apply YAML

Describe pod
Check limits

Display all resources
END
❗ If Something Doesn’t Work
❌ kubectl not working
✔ Check:

kubectl version
❌ Pod not starting
✔ Run:

kubectl describe pod <name>
❌ Image not pulling
✔ Check internet or image name

❌ Service not accessible
✔ Check NodePort

❌ YAML error
✔ Check indentation (VERY IMPORTANT)

🧠 SUPER MEMORY TRICK (WRITE IN EXAM)
👉 Remember this flow:

Jenkins → Automate build
Maven → Build + Test
STS → Develop app
Bugzilla → Report bugs
Kubernetes → Deploy + Scale
🎯 LAST-MINUTE REVISION (1 MIN)
Jenkins = GitHub + Build + Run

Maven = Compile + Test

STS = Run Spring Boot

Bugzilla = Bug report

Kubernetes = Deploy + Scale + Limit resources

If you want next level prep, I can give:
✅ 2-mark / 5-mark / 10-mark answers
✅ Viva questions (very likely asked)
✅ Diagrams you can draw in exam (CI/CD + K8s)




        
        """;