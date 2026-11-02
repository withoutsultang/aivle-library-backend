"""Run explicitly after provisioning. Never place secret values in Terraform state."""
import argparse
import getpass
import secrets


def configure(client, prefix, prompt=input, secret_prompt=getpass.getpass):
    if not prefix.startswith('/') or not prefix.endswith('/'):
        raise ValueError('Parameter prefix must start and end with /.')
    values = {
        'NAVER_CLIENT_ID': prompt('Naver client ID: ').strip(),
        'NAVER_CLIENT_SECRET': secret_prompt('Naver client secret: ').strip(),
        'NAVER_STATE': secrets.token_urlsafe(32),
        'JWT_SECRET': secrets.token_urlsafe(48),
    }
    if not values['NAVER_CLIENT_ID'] or not values['NAVER_CLIENT_SECRET']:
        raise ValueError('Naver settings are required; nothing has been written.')
    image_key = secret_prompt('OpenAI API key (Enter to leave image generation unconfigured): ').strip()
    if image_key:
        values['OPENAI_API_KEY'] = image_key
    for name, value in values.items():
        client.put_parameter(Name=prefix + name, Value=value, Type='SecureString', Overwrite=True)
    return sorted(values)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--region', required=True)
    parser.add_argument('--prefix', required=True)
    args = parser.parse_args()
    # Import the AWS SDK only for a deliberate live configuration run.
    import boto3
    client = boto3.client('ssm', region_name=args.region)
    try:
        names = configure(client, args.prefix)
    except ValueError as error:
        raise SystemExit(str(error)) from None
    except Exception:
        raise SystemExit('AWS parameter update failed. Check permissions; secret values were not logged.') from None
    print('SecureString parameters updated: ' + ', '.join(names))
    print('JWT_SECRET and NAVER_STATE were generated. Updating JWT_SECRET invalidates earlier tokens.')


if __name__ == '__main__':
    main()
