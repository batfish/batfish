## this TF file was used to create the AWS deployment used in the test
## the deployed configuration was then pulled manually using https://github.com/ratulm/bf-aws-snapshot

## to recreate the deployment, change the profile below to something usable.

#################### common ####################
variable "vpc_region_ohio" {
  description = "VPC Region"
  default = "us-east-1"
}
# change profile as necessary. Generally profile is "default"
variable "profile" {
  description = "account profile"
  default = "ratul.org"
}

# change ssh pub key path as appropriate
variable "public_key_path" {
  description = "Public key path"
  default = "~/.ssh/id_rsa.pub"
}
variable "instance_ami_a41" {
  description = "AMI for aws EC2 instance"
  default = "ami-0fc61db8544a617ed"
}
variable "instance_type_t2_micro" {
  description = "type for aws EC2 instance"
  default = "t2.micro"
}

#################### testing ####################
variable "testing" {
  description = "VPC Name"
  default = "testing"
}
variable "testing_cidr" {
  description = "CIDR block for the VPC"
  default = "10.1.0.0/16"
}
variable "testing_public_subnet" {
  description = "public subnet"
  default = "10.1.1.0/24"
}
variable "testing_private_subnet" {
  description = "private subnet"
  default = "10.1.101.0/24"
}
data "aws_availability_zones" "az" {
  state = "available"
}

#################### Provider Config ####################
provider "aws" {
  profile = var.profile
  region = var.vpc_region_ohio
}

# Use existing ssh key pair
resource "aws_key_pair" "ec2key" {
  key_name = "publicKey"
  public_key = file(var.public_key_path)
}

#################### vpc ####################
# Define VPC
resource "aws_vpc" "testing" {
  cidr_block = var.testing_cidr
  enable_dns_support = true
  enable_dns_hostnames = true
  tags = {
    Name = var.testing
  }
}

############# Subnets ############# 
resource "aws_subnet" "testing_public_subnet" {
  vpc_id = aws_vpc.testing.id
  cidr_block = var.testing_public_subnet
  map_public_ip_on_launch = "true"
  availability_zone = data.aws_availability_zones.az.names[0]
  tags = {
    Name = format("%s_%s", var.testing, var.testing_public_subnet)
  }
}
resource "aws_subnet" "testing_private_subnet" {
  vpc_id = aws_vpc.testing.id
  cidr_block = var.testing_private_subnet
  availability_zone = data.aws_availability_zones.az.names[0]
  tags = {
    Name = format("%s_%s", var.testing, var.testing_private_subnet)
  }
}

############# gateways ############# 
# Define internet gateway
resource "aws_internet_gateway" "testing_igw" {
  vpc_id = aws_vpc.testing.id
  tags = {
    Name = var.testing
  }
}
#################### route tables ####################
# Define route table  - igw
resource "aws_route_table" "testing_rtb_igw" {
  vpc_id = aws_vpc.testing.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.testing_igw.id
  }
  tags = {
    Name = format("%s-igw", var.testing)
  }
}
# associate igw route table to the jump subnet
resource "aws_route_table_association" "rtb-asctn-igw-public" {
  route_table_id = aws_route_table.testing_rtb_igw.id
  subnet_id = aws_subnet.testing_public_subnet.id
}

# Define route table  - without igw
resource "aws_route_table" "testing_rtb_noigw" {
  vpc_id = aws_vpc.testing.id
  tags = {
    Name = format("%s-noigw", var.testing)
  }
}
resource "aws_route_table_association" "rtb-asctn-igw-private" {
  route_table_id = aws_route_table.testing_rtb_noigw.id
  subnet_id = aws_subnet.testing_private_subnet.id
}
############# security-group ############# 
# Define security-group
resource "aws_security_group" "testing_sg" {
  name = "testing_sg"
  vpc_id = aws_vpc.testing.id
  # SSH access is controlled at NACL
  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
    description = "SSH Access"
  }
  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
    description = "HTTP Access"
  }
  ingress {
    from_port = 8
    to_port = 0
    protocol = "icmp"
    cidr_blocks = [
      "0.0.0.0/0"]
    description = "ICMP Access"
  }
  ingress {
    from_port = 33434
    to_port = 33534
    protocol = "udp"
    cidr_blocks = [
      "0.0.0.0/0"]
    description = "Traceroute Access"
  }
  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
    description = "Allow all"
  }
  tags = {
    Name = format("%s_general", var.testing)
  }
}
############# NACL ############# 
resource "aws_network_acl" "testing_nacl" {
  vpc_id = aws_vpc.testing.id
  subnet_ids = [
    aws_subnet.testing_public_subnet.id,
    aws_subnet.testing_private_subnet.id
  ]
  ingress {
    protocol = "-1"
    rule_no = 1000
    action = "allow"
    cidr_block = "0.0.0.0/0"
    from_port = 0
    to_port = 0
  }
  egress {
    protocol = "-1"
    rule_no = 1000
    action = "allow"
    cidr_block = "0.0.0.0/0"
    from_port = 0
    to_port = 0
  }
  tags = {
    Name = format("%s-general", var.testing)
  }
}
#################### instances ####################
resource "aws_instance" "instance_public" {
  count = 1
  ami = var.instance_ami_a41
  instance_type = var.instance_type_t2_micro
  key_name = aws_key_pair.ec2key.key_name
  subnet_id = aws_subnet.testing_public_subnet.id
  vpc_security_group_ids = [
    aws_security_group.testing_sg.id]
  user_data = file(var.update-server)
  tags = {
    Name = format("%s-web01", "public")
  }
}

resource "aws_instance" "instance_private" {
  count = 1
  ami = var.instance_ami_a41
  instance_type = var.instance_type_t2_micro
  key_name = aws_key_pair.ec2key.key_name
  subnet_id = aws_subnet.testing_private_subnet.id
  vpc_security_group_ids = [
    aws_security_group.testing_sg.id]
  user_data = file(var.update-server)
  tags = {
    Name = format("%s-web01", "private")
  }
}

################## vpc endpoint interface ###########

resource "aws_vpc_endpoint" "testing_endpoint" {
  vpc_id = aws_vpc.testing.id
  service_name = "com.amazonaws.us-east-1.s3"

  route_table_ids = [
    aws_route_table.testing_rtb_igw.id,
    aws_route_table.testing_rtb_noigw.id]
}



