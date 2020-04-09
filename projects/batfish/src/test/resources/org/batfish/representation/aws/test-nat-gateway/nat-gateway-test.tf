#################### this file defines the setup used for test-load-balancer.
#################### it is not actively used for the test -- the setup was created using it.
#################### then we pulled the AWS data and committed under aws_configs
# change profile as necessary. Generally profile is "default"
variable "profile" {
  default     = "ratul.org"
}
# change ssh pub key path as appropriate
variable "public_key_path" {
  description = "Public key path"
  default = "~/.ssh/id_rsa.pub"
}
variable "vpc_region" {
  description = "VPC Region"
  default     = "us-east-2"
}
variable "vpc_a" {
  description = "VPC Name"
  default     = "vpc_a"
}
variable "vpc_a_cidr" {
  description = "CIDR block for the VPC"
  default     = "10.1.0.0/16"
}
variable "vpc_a_public_subnet" {
  description = "public"
  default     = "10.1.250.0/24"
}
variable "vpc_a_subnet_1" {
  description = "private 1"
  default     = "10.1.1.0/24"
}
variable "vpc_a_subnet_20" {
  description = "private 20"
  default     = "10.1.20.0/24"
}
variable "instance_ami_a41" {
  description = "AMI for aws EC2 instance"
  default     = "ami-02ccb28830b645a41"
}
variable "instance_type" {
  description = "type for aws EC2 instance"
  default     = "t2.micro"
}
#################### Provider ####################
# Define provider
provider "aws" {
  profile = var.profile
  region  = var.vpc_region
}
# Use existing ssh key pair
resource "aws_key_pair" "ec2key" {
  key_name = "publicKey"
  public_key = file(var.public_key_path)
}
#################### vpc ####################
##### setup is one vpc with 3 subnets, one of which is public
##### one instance in each subnet. 
### Define VPC
resource "aws_vpc" "vpc_a" {
  cidr_block = var.vpc_a_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = var.vpc_a
  }
}
### subnets
resource "aws_subnet" "vpc_a_public_subnet" {
  vpc_id     = aws_vpc.vpc_a.id
  cidr_block = var.vpc_a_public_subnet
  map_public_ip_on_launch = true

  tags = {
    Name = "Public Subnet"
  }
}
resource "aws_subnet" "vpc_a_subnet_1" {
  vpc_id     = aws_vpc.vpc_a.id
  cidr_block = var.vpc_a_subnet_1

  tags = {
    Name = "Subnet 1"
  }
}
resource "aws_subnet" "vpc_a_subnet_20" {
  vpc_id     = aws_vpc.vpc_a.id
  cidr_block = var.vpc_a_subnet_20

  tags = {
    Name = "Subnet 20"
  }
}
### Instances
resource "aws_instance" "test1" {
  ami                         = var.instance_ami_a41
  instance_type               = var.instance_type
  key_name = aws_key_pair.ec2key.key_name
  subnet_id = aws_subnet.vpc_a_subnet_1.id
  vpc_security_group_ids = [aws_security_group.vpc_a_sg_jump.id]
  tags = {
    Name = "test1"
  }
}
resource "aws_instance" "test20" {
  ami                         = var.instance_ami_a41
  instance_type               = var.instance_type
  key_name = aws_key_pair.ec2key.key_name
  subnet_id = aws_subnet.vpc_a_subnet_20.id
  vpc_security_group_ids = [aws_security_group.vpc_a_sg_jump.id]
  tags = {
    Name = "test20"
  }
}
resource "aws_instance" "jump_host" {
  ami                         = var.instance_ami_a41
  instance_type               = var.instance_type
  key_name = aws_key_pair.ec2key.key_name
  subnet_id = aws_subnet.vpc_a_public_subnet.id
  vpc_security_group_ids = [aws_security_group.vpc_a_sg_jump.id]
  tags = {
    Name = "Jump Host"
  }
}


##### NAT Gateway
resource "aws_eip" "nat_ip" {
##  instance = "${aws_instance.web.id}"
  vpc      = true
}
resource "aws_nat_gateway" "test_nat" {
  allocation_id = aws_eip.nat_ip.id
  subnet_id     = aws_subnet.vpc_a_public_subnet.id
}


### IGW
resource "aws_internet_gateway" "vpc_a_igw" {
  vpc_id = aws_vpc.vpc_a.id
  tags = {
    Name = var.vpc_a
  }
}


### Route Table to IGW for the public subnet
resource "aws_route_table" "vpc_a_rt_tbl_public" {
  vpc_id = aws_vpc.vpc_a.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.vpc_a_igw.id
  }
  tags = {
    Name =  format("%s_public", var.vpc_a)
  }
}
resource "aws_route_table_association" "vpc_a_rtb_attach_public_subnet" {
  subnet_id      = aws_subnet.vpc_a_public_subnet.id
  route_table_id = aws_route_table.vpc_a_rt_tbl_public.id
}


## Route table to the NAT gateway for subnet1
resource "aws_route_table" "vpc_a_rt_tbl_to_test_nat" {
  vpc_id = aws_vpc.vpc_a.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_nat_gateway.test_nat.id
  }
  tags = {
    Name =  format("%s_public", var.vpc_a)
  }
}
resource "aws_route_table_association" "vpc_a_rtb_attach_to_test_nat" {
  subnet_id      = aws_subnet.vpc_a_subnet_1.id
  route_table_id = aws_route_table.vpc_a_rt_tbl_to_test_nat.id
}


## Define security-group
resource "aws_security_group" "vpc_a_sg_jump" {
  vpc_id = aws_vpc.vpc_a.id
  ingress {
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "SSH Access"
  }
  ingress {
      from_port   = 8
      to_port     = 0
      protocol    = "icmp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "ICMP Access"
  }
 egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all"
  }
  tags = {
    Name =  format("%s_jump", var.vpc_a)
  }
}
resource "aws_security_group" "vpc_a_sg_ssh_only" {
  vpc_id = aws_vpc.vpc_a.id
  ingress {
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "SSH Access"
  }
 egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all"
  }
  tags = {
    Name =  format("%s_ssh", var.vpc_a)
  }
}

## Define a custom network ACL for the public subnet
resource "aws_network_acl" "nacl_public_subnet" {
  vpc_id = aws_vpc.vpc_a.id

  ingress {
    protocol   = -1
    rule_no    = 100
    action     = "allow"
    cidr_block = aws_subnet.vpc_a_subnet_1.cidr_block
    from_port  = 0
    to_port    = 0
  }

  ingress {
    protocol   = -1
    rule_no    = 200
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }

  egress {
    protocol   = -1
    rule_no    = 100
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }

  subnet_ids = [aws_subnet.vpc_a_public_subnet.id]

  tags = {
    Name = "main"
  }
}
