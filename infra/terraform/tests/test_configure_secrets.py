import importlib.util
from pathlib import Path
import unittest
from unittest.mock import Mock

spec = importlib.util.spec_from_file_location('configure_secrets', Path(__file__).parents[1] / 'scripts/configure_secrets.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class ConfigureSecretsTest(unittest.TestCase):
    def test_only_secure_strings_are_written_and_return_value_has_names_only(self):
        client = Mock()
        hidden = iter(['fixture-client-secret', 'fixture-image-key'])
        names = module.configure(client, '/test/runtime/', lambda _: 'fixture-client', lambda _: next(hidden))
        self.assertEqual(len(names), 5)
        self.assertNotIn('fixture-image-key', names)
        for call in client.put_parameter.call_args_list:
            self.assertEqual(call.kwargs['Type'], 'SecureString')
            self.assertTrue(call.kwargs['Name'].startswith('/test/runtime/'))
        jwt = next(call.kwargs['Value'] for call in client.put_parameter.call_args_list if call.kwargs['Name'].endswith('JWT_SECRET'))
        self.assertGreaterEqual(len(jwt), 32)

    def test_missing_naver_configuration_writes_nothing(self):
        client = Mock()
        with self.assertRaises(ValueError):
            module.configure(client, '/test/runtime/', lambda _: '', lambda _: '')
        client.put_parameter.assert_not_called()

    def test_image_provider_can_be_left_unconfigured(self):
        client = Mock()
        hidden = iter(['fixture-client-secret', ''])
        names = module.configure(client, '/test/runtime/', lambda _: 'fixture-client', lambda _: next(hidden))
        self.assertNotIn('OPENAI_API_KEY', names)

    def test_invalid_parameter_prefix_is_rejected_before_input(self):
        client = Mock()
        with self.assertRaises(ValueError):
            module.configure(client, 'wrong')
        client.put_parameter.assert_not_called()


if __name__ == '__main__':
    unittest.main()
