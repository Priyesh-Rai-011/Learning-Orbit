## 1) Does EKS Control Plane come under our VPC?

✅ EKS Control Plane is managed by AWS and runs in an AWS-managed VPC (not your VPC).

But…

✅ When you create the EKS cluster, you select your VPC + subnets because AWS creates ENIs (network interfaces) inside your subnets to allow communication between:

your worker nodes (EC2)

your control plane endpoint

So control plane is not inside your VPC, but it connects to your VPC using ENIs.

That’s why you must provide subnets.

## 2) Where do Nodes / Pods run?

✅ Node Groups (EC2 instances) run inside your VPC, inside the subnets you select.

Pods run on nodes → so indirectly pods also run in those subnets.

Usually:

Private subnets = Worker nodes + Pods

Public subnet = NAT Gateway + Load Balancer (sometimes)

## 3) Internet Gateway (IGW) stays where?

✅ Internet Gateway is attached to the VPC.

It is not inside a subnet.

It is like a “VPC-level door” to the internet.

📌 IGW = VPC component

## 4) NAT Gateway stays where?

✅ NAT Gateway is created inside a Public Subnet.

Because NAT Gateway itself needs internet access, so it must sit in a subnet that has a route to IGW.

📌 NAT Gateway = Subnet-level resource (public subnet only)

Also NAT Gateway gets:

an Elastic IP (EIP)

and uses IGW to go to internet

## 5) Route tables are created where?

✅ Route Tables are created inside the VPC.

They are not inside a subnet, but they are associated with subnets.

📌 Route Table = VPC component
📌 Association = Subnet-level mapping

## 6) Public Route Table vs Private Route Table
✅ Public Route Table (associated with public subnet)

Contains:

Destination: 0.0.0.0/0

Target: Internet Gateway (IGW)

Meaning: anything in that subnet can directly reach internet.

✅ Private Route Table (associated with private subnets)

Contains:

Destination: 0.0.0.0/0

Target: NAT Gateway

Meaning: instances/pods can reach internet only outbound, not inbound.

## 7) How does route table association work?

Each subnet must be associated with exactly one route table (main/default or custom).

Example:

Public Subnet Association:

PublicSubnet → PublicRouteTable

Private Subnet Associations:

PrivateSubnet1 → PrivateRouteTable
PrivateSubnet2 → PrivateRouteTable

So yes, one private route table can be used for multiple private subnets.

## 8) Actual Flow (very important)
Case A: Pod/Node in private subnet wants internet (yum update, docker pull, etc.)

Flow:

Private EC2/Pod
→ Private Route Table (0.0.0.0/0 → NAT Gateway)
→ NAT Gateway (in Public subnet)
→ Public Route Table (0.0.0.0/0 → IGW)
→ IGW
→ Internet

So NAT is like a “proxy exit door”.

Case B: Internet user wants to access your application in EKS

They cannot directly reach private subnets.

So flow is usually:

Internet
→ IGW
→ Public Subnet Load Balancer (ALB/NLB created by Kubernetes service type LoadBalancer / Ingress Controller)
→ Target Group
→ Nodes/Pods in private subnet

So the Load Balancer is public-facing.

## 9) Bastion Host concept (your doubt)

✅ Bastion host is optional.

It is usually created in public subnet.

Purpose:

SSH into private EC2 nodes (if needed)

access internal resources

But in EKS world, you usually access the cluster using:

kubectl from your laptop

or from a jump server (bastion)

or via AWS SSM Session Manager (better than bastion)

So bastion is not mandatory.

10) Clean Final Architecture Summary
VPC

Contains:

Subnets

Route tables

IGW attached to it

Public Subnet

Contains:

NAT Gateway

ALB/NLB (optional but common)

Bastion host (optional)

Associated Route Table:

0.0.0.0/0 → IGW

Private Subnets (2 subnets recommended for HA)

Contain:

EKS Worker Nodes

Pods

Associated Route Table:

0.0.0.0/0 → NAT Gateway

## 11) Your understanding correction (important)

❌ You said:
“private route table helps private subnet connect to public subnet”

Not exactly.

✅ Private subnet does NOT route to public subnet.

It routes to NAT Gateway (which happens to be in public subnet).

So correct statement:

➡️ Private subnet routes to NAT Gateway
➡️ NAT Gateway routes to IGW

## 12) Best mental diagram
```
           Internet
              |
             IGW   (attached to VPC)
              |
      -------------------
      |   Public Subnet |
      |  NAT Gateway    |
      |  ALB (optional) |
      -------------------
              |
       Private Route Table
     (0.0.0.0/0 -> NAT)
              |
   -------------------------
   | Private Subnet 1      |
   | Worker Nodes + Pods   |
   -------------------------
   -------------------------
   | Private Subnet 2      |
   | Worker Nodes + Pods   |
   -------------------------
```

Final Answers to your questions (direct)
✅ Where does NAT gateway stay?

➡️ Inside Public Subnet

✅ Where does internet gateway stay?

➡️ Attached to VPC (not subnet)

✅ Where do we create public route table?

➡️ In the VPC, then associate with public subnet

✅ Where do we create private route table?

➡️ In the VPC, then associate with private subnets

✅ How is route table association configured?

➡️ You explicitly associate each subnet with one route table:

Public subnet → Public RT

Private subnets → Private RT

✅ How does it flow?

Private subnet → NAT → IGW → Internet
Internet → IGW → ALB (public subnet) → Pods (private subnet)