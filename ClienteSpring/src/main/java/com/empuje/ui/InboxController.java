package com.empuje.ui;

import com.empuje.grpc.ong.ListEventosExternosResponse;
import com.empuje.grpc.ong.ListSolicitudesExternasResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InboxController {

    private final InboxService inbox;

    public InboxController(InboxService inbox) {
        this.inbox = inbox;
    }

    @GetMapping("/inbox")
    public String inbox(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        ListSolicitudesExternasResponse sols = inbox.listarSolicitudes(page, size);
        ListEventosExternosResponse evs = inbox.listarEventos(page, size);
        model.addAttribute("sols", sols.getDataList());
        model.addAttribute("evs", evs.getDataList());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("hasMoreS", sols.getHasMore());
        model.addAttribute("hasMoreE", evs.getHasMore());
        return "admin/inbox"; // templates/admin/inbox.html
    }
}
