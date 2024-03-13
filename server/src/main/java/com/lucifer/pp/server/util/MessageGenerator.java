package com.lucifer.pp.server.util;

import com.lucifer.pp.net.data.PPMessage;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.StatusEnum;

public class MessageGenerator {
    public static PPProtocol<PPMessage> generate(PPProtocolEnum originProtocol,StatusEnum statusEnum,String message){
        PPMessage ppMessage = new PPMessage(originProtocol,statusEnum,message);
        return new PPProtocol<>(PPProtocolEnum.MESSAGE, ppMessage);
    }
}
