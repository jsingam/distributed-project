class Neighbour{
    private String ip;
    private int port;

    public Neighbour(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public String getIp(){
        return this.ip;
    }


    public int getPort(){
        return this.port;
    }

    @Override
    public String toString() {
        return "ip : "+ip+"    port : "+String.valueOf(port);


    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Neighbour){
            Neighbour neighbour=(Neighbour)obj;
            if(this.ip.equals(neighbour.getIp()) && this.port==neighbour.getPort()){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return port+10000*Integer.parseInt(ip.replace(".",""));
    }

    public Neighbour clone(){
        return new Neighbour(ip,port);
    }
}
