package com.slopeoasis.user.service;

import org.springframework.stereotype.Service;

import com.slopeoasis.user.entity.VrednostX;
import com.slopeoasis.user.repository.VrednostXRepo;

import jakarta.annotation.PostConstruct;

@Service
public class VrednostXServ {
    private final VrednostXRepo vredXRe;

    public VrednostXServ(VrednostXRepo vredXRe){
        this.vredXRe = vredXRe;
    }

    @PostConstruct
    public void init(){
        if(vredXRe.count() == 0){//vrne število vrstic entitet v PB
            vredXRe.save(new VrednostX());//če je ni jo kreira in shran na PB
        }
    }

    public VrednostX getVredX(){
        return vredXRe.findById(1).orElseThrow();
    }

    public VrednostX updateVredX(int novaVrednost){
        VrednostX vrednostX = getVredX();//vrne ta pravo instanco glede na id=1
        vrednostX.setXVrednost(novaVrednost);//setXVrednost iz entity
        return vredXRe.save(vrednostX);//shran na bazo in vrne
    }

    public VrednostX incVredX(){
        VrednostX vrednostX = getVredX();//vrne ta pravo instanco glede na id=1
        vrednostX.setXVrednost(vrednostX.getXVrednost()+1);//dobi trenutno in incrementa
        return vredXRe.save(vrednostX);
    }
}
