## this TF file was used to create the AWS deployment used in the test
## the deployed configuration was then pulled manually using https://github.com/ratulm/bf-aws-snapshot

## to recreate the deployment, change the profiles below to something usable.
variable "profile-bat" {
  default = "AwsBetaSandboxAdmin"
}
variable "profile-fish" {
  default = "AwsBetaSandbox2Admin"
}

// regions
variable "region-bat" {
  default = "us-east-1"
}
variable "region-fish" {
  default = "us-west-1"
}

// providers and caller identity
provider "aws" {
  alias = "bat"
  profile = var.profile-bat
  region = var.region-bat
}
provider "aws" {
  alias = "fish"
  profile = var.profile-fish
  region = var.region-fish
}
data "aws_caller_identity" "bat" {
  provider = aws.bat
}
data "aws_caller_identity" "fish" {
  provider = aws.fish
}

// instance configuration
variable "public_key_path" {
  default = "~/.ssh/id_rsa.pub"
}
resource "aws_key_pair" "bat" {
  provider = aws.bat
  key_name = "publicKey"
  public_key = file(var.public_key_path)
}
resource "aws_key_pair" "fish" {
  provider = aws.fish
  key_name = "publicKey"
  public_key = file(var.public_key_path)
}
variable "instance-type" {
  default = "t2.micro"
}
variable "ami-region-bat" {
  default = "ami-0fc61db8544a617ed"
}
variable "ami-region-fish" {
  default = "ami-09a7fe78668f1e2c0"
}

// cidrs and ips
variable "cidr-vpc-bat" {
  default = "10.10.0.0/16"
}
variable "cidr-subnet-bat" {
  default = "10.10.1.0/24"
}
variable "ip-bat" {
  default = "10.10.1.100"
}
variable "cidr-vpc-bat2" {
  default = "10.20.0.0/16"
}
variable "cidr-subnet-bat2" {
  default = "10.20.1.0/24"
}
variable "ip-bat2" {
  default = "10.20.1.100"
}
variable "cidr-vpc-fish" {
  default = "192.168.0.0/16"
}
variable "cidr-subnet-fish" {
  default = "192.168.1.0/24"
}
variable "ip-fish" {
  default = "192.168.1.100"
}


// availability zones
data "aws_availability_zones" "bat" {
  provider = aws.bat
  state = "available"
}
data "aws_availability_zones" "fish" {
  provider = aws.fish
  state = "available"
}

// VPCs
resource "aws_vpc" "bat" {
  provider = aws.bat

  cidr_block = var.cidr-vpc-bat
  enable_dns_support = true
  enable_dns_hostnames = true

  tags = {
    Name = "bat"
  }
}
resource "aws_vpc" "bat2" {
  provider = aws.bat

  cidr_block = var.cidr-vpc-bat2
  enable_dns_support = true
  enable_dns_hostnames = true

  tags = {
    Name = "bat2"
  }
}
resource "aws_vpc" "fish" {
  provider = aws.fish

  cidr_block = var.cidr-vpc-fish
  enable_dns_support = true
  enable_dns_hostnames = true

  tags = {
    Name = "fish"
  }
}

// subnets
resource "aws_subnet" "bat" {
  provider = aws.bat

  vpc_id = aws_vpc.bat.id
  cidr_block = var.cidr-subnet-bat
  map_public_ip_on_launch = "true"
  availability_zone = data.aws_availability_zones.bat.names[0]

  tags = {
    Name = "bat"
  }
}
resource "aws_subnet" "bat2" {
  provider = aws.bat

  vpc_id = aws_vpc.bat2.id
  cidr_block = var.cidr-subnet-bat2
  map_public_ip_on_launch = "true"
  availability_zone = data.aws_availability_zones.bat.names[0]

  tags = {
    Name = "bat2"
  }
}
resource "aws_subnet" "fish" {
  provider = aws.fish

  vpc_id = aws_vpc.fish.id
  cidr_block = var.cidr-subnet-fish
  map_public_ip_on_launch = "true"
  availability_zone = data.aws_availability_zones.fish.names[0]

  tags = {
    Name = "fish"
  }
}

// instances
resource "aws_instance" "bat" {
  provider = aws.bat

  ami = var.ami-region-bat
  instance_type = var.instance-type
  key_name = aws_key_pair.bat.key_name
  private_ip = var.ip-bat
  subnet_id = aws_subnet.bat.id
  vpc_security_group_ids = [
    aws_security_group.bat.id]

  tags = {
    Name = "bat"
  }
}
resource "aws_instance" "bat2" {
  provider = aws.bat

  ami = var.ami-region-bat
  instance_type = var.instance-type
  key_name = aws_key_pair.bat.key_name
  private_ip = var.ip-bat2
  subnet_id = aws_subnet.bat2.id
  vpc_security_group_ids = [
    aws_security_group.bat2.id]

  tags = {
    Name = "bat2"
  }
}
resource "aws_instance" "fish" {
  provider = aws.fish

  ami = var.ami-region-fish
  instance_type = var.instance-type
  key_name = aws_key_pair.fish.key_name
  private_ip = var.ip-fish
  subnet_id = aws_subnet.fish.id
  vpc_security_group_ids = [
    aws_security_group.fish.id]

  tags = {
    Name = "fish"
  }
}

// security groups
resource "aws_security_group" "bat" {
  provider = aws.bat

  vpc_id = aws_vpc.bat.id
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
}
resource "aws_security_group" "bat2" {
  provider = aws.bat

  vpc_id = aws_vpc.bat2.id
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
}
resource "aws_security_group" "fish" {
  provider = aws.fish

  vpc_id = aws_vpc.fish.id
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
}

// internet gateways
resource "aws_internet_gateway" "bat" {
  provider = aws.bat
  vpc_id = aws_vpc.bat.id
}
// internet gateways
resource "aws_internet_gateway" "bat2" {
  provider = aws.bat
  vpc_id = aws_vpc.bat2.id
}
resource "aws_internet_gateway" "fish" {
  provider = aws.fish
  vpc_id = aws_vpc.fish.id
}

// transit gateways
resource "aws_ec2_transit_gateway" "bat" {
  provider = aws.bat
}
resource "aws_ec2_transit_gateway" "fish" {
  provider = aws.fish
}

// attach VPCs to the TGWs
resource "aws_ec2_transit_gateway_vpc_attachment" "bat" {
  provider = aws.bat

  subnet_ids = [
    aws_subnet.bat.id]
  depends_on = [
    aws_subnet.bat]
  transit_gateway_id = aws_ec2_transit_gateway.bat.id
  vpc_id = aws_vpc.bat.id

  tags = {
    Name = "bat"
  }
}
resource "aws_ec2_transit_gateway_vpc_attachment" "bat2" {
  provider = aws.bat

  subnet_ids = [
    aws_subnet.bat2.id]
  depends_on = [
    aws_subnet.bat2]
  transit_gateway_id = aws_ec2_transit_gateway.bat.id
  vpc_id = aws_vpc.bat2.id

  transit_gateway_default_route_table_association = false
  transit_gateway_default_route_table_propagation = false

  tags = {
    Name = "bat2"
  }
}
resource "aws_ec2_transit_gateway_vpc_attachment" "fish" {
  provider = aws.fish

  subnet_ids = [
    aws_subnet.fish.id]
  depends_on = [
    aws_subnet.fish]
  transit_gateway_id = aws_ec2_transit_gateway.fish.id
  vpc_id = aws_vpc.fish.id

  tags = {
    Name = "fish"
  }
}


// Share TGW bat with fish
resource "aws_ram_resource_share" "bat" {
  provider = aws.bat
  name = "resource share bat"
}
resource "aws_ram_resource_association" "bat" {
  provider = aws.bat

  resource_arn = aws_ec2_transit_gateway.bat.arn
  resource_share_arn = aws_ram_resource_share.bat.id
}
resource "aws_ram_principal_association" "bat" {
  provider = aws.bat

  principal = data.aws_caller_identity.fish.account_id
  resource_share_arn = aws_ram_resource_share.bat.id
}

resource "aws_ec2_transit_gateway_peering_attachment" "bat-fish" {
  provider = aws.fish

  peer_account_id = aws_ec2_transit_gateway.bat.owner_id
  peer_region = var.region-bat
  peer_transit_gateway_id = aws_ec2_transit_gateway.bat.id
  transit_gateway_id = aws_ec2_transit_gateway.fish.id

  depends_on = [
    aws_ram_principal_association.bat,
    aws_ram_resource_association.bat]

  tags = {
    Name = "bat-fish"
  }
}

resource "aws_ec2_transit_gateway_peering_attachment_accepter" "bat-fish" {
  provider = aws.bat

  transit_gateway_attachment_id = aws_ec2_transit_gateway_peering_attachment.bat-fish.id

  tags = {
    Name = "bat-fish"
  }
}

// TGW route table for bat2
resource "aws_ec2_transit_gateway_route_table" "bat2" {
  provider = aws.bat

  transit_gateway_id = aws_ec2_transit_gateway.bat.id

  tags = {
    Name = "bat2"
  }
}
resource "aws_ec2_transit_gateway_route_table_association" "bat2" {
  provider = aws.bat

  transit_gateway_attachment_id = aws_ec2_transit_gateway_vpc_attachment.bat2.id
  transit_gateway_route_table_id = aws_ec2_transit_gateway_route_table.bat2.id
}
resource "aws_ec2_transit_gateway_route_table_propagation" "bat2" {
  provider = aws.bat

  transit_gateway_attachment_id = aws_ec2_transit_gateway_vpc_attachment.bat2.id
  transit_gateway_route_table_id = aws_ec2_transit_gateway_route_table.bat2.id
}
# lack of this propgation break flows from fish to bat2
# resource "aws_ec2_transit_gateway_route_table_propagation" "bat2-default" {
#   provider = aws.bat

#   transit_gateway_attachment_id  = aws_ec2_transit_gateway_vpc_attachment.bat2.id
#   transit_gateway_route_table_id = aws_ec2_transit_gateway.bat.association_default_route_table_id
# }

// TGW static routes
resource "aws_ec2_transit_gateway_route" "bat2-to-fish" {
  provider = aws.bat

  destination_cidr_block = var.cidr-vpc-fish
  transit_gateway_attachment_id = aws_ec2_transit_gateway_peering_attachment.bat-fish.id
  transit_gateway_route_table_id = aws_ec2_transit_gateway_route_table.bat2.id

  depends_on = [
    aws_ec2_transit_gateway_peering_attachment_accepter.bat-fish]
}
resource "aws_ec2_transit_gateway_route" "fish-to-bat2" {
  provider = aws.fish

  destination_cidr_block = var.cidr-vpc-bat2
  transit_gateway_attachment_id = aws_ec2_transit_gateway_peering_attachment.bat-fish.id
  transit_gateway_route_table_id = aws_ec2_transit_gateway.fish.association_default_route_table_id

  depends_on = [
    aws_ec2_transit_gateway_peering_attachment_accepter.bat-fish]
}

# Subnet route tables and associations
resource "aws_route_table" "bat" {
  provider = aws.bat

  vpc_id = aws_vpc.bat.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.bat.id
  }
  route {
    cidr_block = var.cidr-vpc-fish
    transit_gateway_id = aws_ec2_transit_gateway.bat.id
  }
}
resource "aws_route_table" "bat2" {
  provider = aws.bat

  vpc_id = aws_vpc.bat2.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.bat2.id
  }
  route {
    cidr_block = var.cidr-vpc-fish
    transit_gateway_id = aws_ec2_transit_gateway.bat.id
  }
}
resource "aws_route_table_association" "bat" {
  provider = aws.bat

  route_table_id = aws_route_table.bat.id
  subnet_id = aws_subnet.bat.id
}
resource "aws_route_table_association" "bat2" {
  provider = aws.bat

  route_table_id = aws_route_table.bat2.id
  subnet_id = aws_subnet.bat2.id
}
resource "aws_route_table" "fish" {
  provider = aws.fish

  vpc_id = aws_vpc.fish.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.fish.id
  }
  route {
    cidr_block = var.cidr-vpc-bat
    transit_gateway_id = aws_ec2_transit_gateway.fish.id
  }
  route {
    cidr_block = var.cidr-vpc-bat2
    transit_gateway_id = aws_ec2_transit_gateway.fish.id
  }
}
resource "aws_route_table_association" "fish" {
  provider = aws.fish

  route_table_id = aws_route_table.fish.id
  subnet_id = aws_subnet.fish.id
}





