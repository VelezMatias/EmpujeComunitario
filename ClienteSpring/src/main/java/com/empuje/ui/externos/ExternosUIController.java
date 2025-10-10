package com.empuje.ui.externos;

import com.empuje.grpc.ong.ListExternasRequest;
import com.empuje.grpc.ong.OngServiceGrpc;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ui/externos")
public class ExternosUIController {

    private final OngServiceGrpc.OngServiceBlockingStub ong;

    public ExternosUIController(OngServiceGrpc.OngServiceBlockingStub ong) {
        this.ong = ong;
    }

    @GetMapping("/eventos")
    public String eventos(Model model) {
        var req = ListExternasRequest.newBuilder().setPage(1).setPageSize(50).build();
        var resp = ong.listEventosExternos(req);
        model.addAttribute("items", resp.getDataList());
        return "externos/eventos";
    }
}
