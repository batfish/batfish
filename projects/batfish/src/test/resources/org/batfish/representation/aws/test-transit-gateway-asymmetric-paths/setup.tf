## this TF file was used to create the AWS deployment used in the test
## the deployed configuration was then pulled manually using https://github.com/ratulm/bf-aws-snapshot

## to recreate the deployment, change the profile below to something usable.

#################### common ####################
variable "vpc_region_ohio" {
  description = "VPC Region"
  default     = "us-east-1"
}
# change profile as necessary. Generally profile is "default"
variable "profile" {
  description = "account profile"
  default     = "ratul.org"  ## change this
}

# change ssh pub key path as appropriate
variable "public_key_path" {
  description = "Public key path"
  default = "~/.ssh/id_rsa.pub"
}
variable "instance_ami_a41" {
  description = "AMI for aws EC2 instance"
  default     = "ami-0fc61db8544a617ed"
}
variable "instance_type_t2_micro" {
  description = "type for aws EC2 instance"
  default     = "t2.micro"
}
#################### bat ####################
variable "bat" {
  description = "VPC Name"
  default     = "bat"
}
variable "bat_cidr" {
  description = "CIDR block for the VPC"
  default     = "10.1.0.0/16"
}
variable "bat_public_subnet" {
  description = "public subnet"
  default     = "10.1.1.0/24"
}
data "aws_availability_zones" "az" {
  state = "available"
}

#################### Provider Config ####################
provider "aws" {
  profile = var.profile
  region  = var.vpc_region_ohio
}

# Use existing ssh key pair
resource "aws_key_pair" "ec2key" {
  key_name = "publicKey"
  public_key = file(var.public_key_path)
}

#################### vpc - bat ####################
# Define VPC
resource "aws_vpc" "bat" {
  cidr_block = var.bat_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = var.bat
  }
}

############# Subnets ############# 
resource "aws_subnet" "bat_public_subnet" {
  vpc_id     = aws_vpc.bat.id
  cidr_block = var.bat_public_subnet
  map_public_ip_on_launch = "true"
  availability_zone = data.aws_availability_zones.az.names[0]
  tags = {
    Name = format("%s_%s", var.bat, var.bat_public_subnet)
  }
}

############# gateways ############# 
# Define internet gateway
resource "aws_internet_gateway" "bat_igw" {
  vpc_id = aws_vpc.bat.id
  tags = {
    Name = var.bat
  }
}
#################### route tables ####################
# Define route table  - igw
resource "aws_route_table" "bat_rtb_igw" {
  vpc_id = aws_vpc.bat.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.bat_igw.id
  }
  route {
    cidr_block = "10.2.0.0/16"
    transit_gateway_id = aws_ec2_transit_gateway.tgw-bat.id
  }
  tags = {
    Name = format("%s-igw", var.bat)
  }
}
# associate igw route table to the jump subnet
resource "aws_route_table_association" "rtb-asctn-igw-public-bat" {
  route_table_id = aws_route_table.bat_rtb_igw.id
  subnet_id      = aws_subnet.bat_public_subnet.id
}
############# security-group ############# 
# Define security-group
resource "aws_security_group" "bat_sg" {
  name = "bat_sg"
  vpc_id = aws_vpc.bat.id
  # SSH access is controlled at NACL
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
  ingress {
      from_port   = 33434
      to_port     = 33534
      protocol    = "udp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "Traceroute Access"
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all"
  }
  tags = {
    Name =  format("%s_general", var.bat)
  }
}
############# NACL ############# 
resource "aws_network_acl" "bat_nacl" {
  vpc_id = aws_vpc.bat.id
  subnet_ids = [
    aws_subnet.bat_public_subnet.id
  ]
  ingress {
    protocol   = "-1"
    rule_no    = 1000
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }
  egress {
    protocol   = "-1"
    rule_no    = 1000
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }
  tags = {
    Name =  format("%s-general", var.bat)
  }
}
#################### instances ####################
resource "aws_instance" "bat_web01" {
  count = 1
  ami                         = var.instance_ami_a41
  instance_type               = var.instance_type_t2_micro
  key_name = aws_key_pair.ec2key.key_name
  private_ip                  = "10.1.1.100"
  subnet_id = aws_subnet.bat_public_subnet.id
  vpc_security_group_ids = [aws_security_group.bat_sg.id]
  tags = {
    Name =  format("%s-web01", var.bat)
  }
}


#################### fish ####################
variable "fish" {
  description = "VPC Name"
  default     = "fish"
}
variable "fish_cidr" {
  description = "CIDR block for the VPC"
  default     = "10.2.0.0/16"
}
variable "fish_public_subnet" {
  description = "public subnet"
  default     = "10.2.1.0/24"
}

#################### vpc - fish ####################
# Define VPC
resource "aws_vpc" "fish" {
  cidr_block = var.fish_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = var.fish
  }
}

############# Subnets ############# 
resource "aws_subnet" "fish_public_subnet" {
  vpc_id     = aws_vpc.fish.id
  cidr_block = var.fish_public_subnet
  map_public_ip_on_launch = "true"
  availability_zone = data.aws_availability_zones.az.names[0]
  tags = {
    Name = format("%s_%s", var.fish, var.fish_public_subnet)
  }
}

############# gateways ############# 
# Define internet gateway
resource "aws_internet_gateway" "fish_igw" {
  vpc_id = aws_vpc.fish.id
  tags = {
    Name = var.fish
  }
}
#################### route tables ####################
# Define route table  - igw
resource "aws_route_table" "fish_rtb_igw" {
  vpc_id = aws_vpc.fish.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.fish_igw.id
  }
  route {
    cidr_block = "10.1.0.0/16"
    transit_gateway_id = aws_ec2_transit_gateway.tgw-fish.id
  }
  tags = {
    Name = format("%s-igw", var.fish)
  }
}
# associate igw route table to the jump subnet
resource "aws_route_table_association" "rtb-asctn-igw-public-fish" {
  route_table_id = aws_route_table.fish_rtb_igw.id
  subnet_id      = aws_subnet.fish_public_subnet.id
}
############# security-group ############# 
# Define security-group
resource "aws_security_group" "fish_sg" {
  name = "fish_sg"
  vpc_id = aws_vpc.fish.id
  # SSH access is controlled at NACL
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
  ingress {
      from_port   = 33434
      to_port     = 33534
      protocol    = "udp"
      cidr_blocks = ["0.0.0.0/0"]
      description = "Traceroute Access"
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all"
  }
  tags = {
    Name =  format("%s_general", var.fish)
  }
}
############# NACL ############# 
resource "aws_network_acl" "fish_nacl" {
  vpc_id = aws_vpc.fish.id
  subnet_ids = [
    aws_subnet.fish_public_subnet.id
  ]
  ingress {
    protocol   = "-1"
    rule_no    = 1000
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }
  egress {
    protocol   = "-1"
    rule_no    = 1000
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }
  tags = {
    Name =  format("%s-general", var.fish)
  }
}
#################### instances ####################
resource "aws_instance" "fish_web01" {
  count = 1
  ami                         = var.instance_ami_a41
  instance_type               = var.instance_type_t2_micro
  key_name = aws_key_pair.ec2key.key_name
  private_ip                  = "10.2.1.100"
  subnet_id = aws_subnet.fish_public_subnet.id
  vpc_security_group_ids = [aws_security_group.fish_sg.id]
  tags = {
    Name =  format("%s-web01", var.fish)
  }
}

#################### TGW bat ####################
# Create TGW
resource "aws_ec2_transit_gateway" "tgw-bat" {
  tags = {
        Name = format("tgw-bat")
    }
}
# Route table
resource "aws_ec2_transit_gateway_route_table" "tgw-bat-rtb" {
  transit_gateway_id = aws_ec2_transit_gateway.tgw-bat.id
    tags = {
        Name = format("tgw-bat-rtb")
    }
}
# tgw-bat --> vpc_bat attachement
resource "aws_ec2_transit_gateway_vpc_attachment" "tgw-bat-vpc-bat-att" {
  subnet_ids         = [aws_subnet.bat_public_subnet.id]
  depends_on         = [aws_subnet.bat_public_subnet]
  transit_gateway_id = aws_ec2_transit_gateway.tgw-bat.id
  vpc_id             = aws_vpc.bat.id
  transit_gateway_default_route_table_association = "false"
  transit_gateway_default_route_table_propagation = "false"
  tags               = {
    Name = format("tgw-bat-vpc-bat-att")
  }
}
# tgw-bat --> vpc-fish attachement
resource "aws_ec2_transit_gateway_vpc_attachment" "tgw-bat-vpc-fish-att" {
  subnet_ids         = [aws_subnet.fish_public_subnet.id]
  depends_on         = [aws_subnet.fish_public_subnet]
  transit_gateway_id = aws_ec2_transit_gateway.tgw-bat.id
  vpc_id             = aws_vpc.fish.id
  transit_gateway_default_route_table_association = "false"
  transit_gateway_default_route_table_propagation = "false"
  tags               = {
    Name = format("tgw-bat-vpc-fish-att")
  }
}
# Propogation and association
resource "aws_ec2_transit_gateway_route_table_association" "tgw-bat-vpc-bat-asctn" {
  transit_gateway_attachment_id  = aws_ec2_transit_gateway_vpc_attachment.tgw-bat-vpc-bat-att.id
  transit_gateway_route_table_id = aws_ec2_transit_gateway_route_table.tgw-bat-rtb.id
}
resource "aws_ec2_transit_gateway_route_table_propagation" "tgw-bat-vpc-fish-prop" {
  transit_gateway_attachment_id  = aws_ec2_transit_gateway_vpc_attachment.tgw-bat-vpc-fish-att.id
  transit_gateway_route_table_id = aws_ec2_transit_gateway_route_table.tgw-bat-rtb.id
}


#################### TGW fish ####################
# Create TGW
resource "aws_ec2_transit_gateway" "tgw-fish" {
  tags = {
        Name = format("tgw-fish")
    }
}
# Route table
resource "aws_ec2_transit_gateway_route_table" "tgw-fish-rtb" {
  transit_gateway_id = aws_ec2_transit_gateway.tgw-fish.id
    tags = {
        Name = format("tgw-fish-rtb")
    }
}
# tgw-fish --> vpc_bat attachement
resource "aws_ec2_transit_gateway_vpc_attachment" "tgw-fish-vpc-bat-att" {
  subnet_ids         = [aws_subnet.bat_public_subnet.id]
  depends_on         = [aws_subnet.bat_public_subnet]
  transit_gateway_id = aws_ec2_transit_gateway.tgw-fish.id
  vpc_id             = aws_vpc.bat.id
  transit_gateway_default_route_table_association = "false"
  transit_gateway_default_route_table_propagation = "false"
  tags               = {
    Name = format("tgw-fish-vpc-bat-att")
  }
}

# tgw-fish --> vpc_fish attachement
resource "aws_ec2_transit_gateway_vpc_attachment" "tgw-fish-vpc-fish-att" {
  subnet_ids         = [aws_subnet.fish_public_subnet.id]
  depends_on         = [aws_subnet.fish_public_subnet]
  transit_gateway_id = aws_ec2_transit_gateway.tgw-fish.id
  vpc_id             = aws_vpc.fish.id
  transit_gateway_default_route_table_association = "false"
  transit_gateway_default_route_table_propagation = "false"
  tags               = {
    Name = format("tgw-fish-vpc-fish-att")
  }
}
# Propogation and association
resource "aws_ec2_transit_gateway_route_table_propagation" "tgw-fish-vpc-bat-prop" {
  transit_gateway_attachment_id  = aws_ec2_transit_gateway_vpc_attachment.tgw-fish-vpc-bat-att.id
  transit_gateway_route_table_id = aws_ec2_transit_gateway_route_table.tgw-fish-rtb.id
}
resource "aws_ec2_transit_gateway_route_table_association" "tgw-fish-vpc-fish-asctn" {
  transit_gateway_attachment_id  = aws_ec2_transit_gateway_vpc_attachment.tgw-fish-vpc-fish-att.id
  transit_gateway_route_table_id = aws_ec2_transit_gateway_route_table.tgw-fish-rtb.id
}










