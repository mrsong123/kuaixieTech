import unittest
from audit_routes import routes

class RouteExtractionTests(unittest.TestCase):
    def test_empty_method_path_and_base(self):
        self.assertEqual([('GET','/me/products'),('PUT','/me/products/{id}')],routes('@RestController @RequestMapping("/api/me/products") class Test { @GetMapping public Object list() {} @PutMapping("/{id}") public void edit(){} }'))
    def test_full_paths_without_class_prefix(self):
        self.assertEqual([('POST','/me/payments')],routes('@RestController class Test { @PostMapping("/api/me/payments") public void create(){} }'))
    def test_comments_are_not_routes(self):
        self.assertEqual([],routes('@RestController class Test { /* @GetMapping("/fake") */\n // @PostMapping("/fake")\n }'))
    def test_named_paths(self):
        self.assertEqual([('GET','/x')],routes('@RestController class Test { @GetMapping(path="/api/x") public void x(){} }'))
    def test_unsupported_arrays_fail_visibly(self):
        with self.assertRaises(ValueError): routes('@RestController class Test { @GetMapping({"/a","/b"}) public void x(){} }')

if __name__ == '__main__': unittest.main()
